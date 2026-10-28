# Plume Admin Security

## Plume Admin general use

For general use, please check the [main Plume Admin documentation](https://github.com/Coreoz/Plume-admin)


## Creating my own permission workflow using Plume

For general use [WebSessionAdmin](src/main/java/com/coreoz/plume/admin/websession/WebSessionAdmin.java) is provided, but you might want to add other fields in your JWT token.
This guide describes how to handle your own JWT token based session and how to secure your API: this way API endpoints you configure will require a valid JWT session in order to be accessed.

Since 5.2, you describe your session with a
[WebSessionSecurityConfig](src/main/java/com/coreoz/plume/admin/websession/WebSessionSecurityConfig.java)
and register it manually in Jersey (it is a JAX-RS `Feature`). You no longer need to write your own
`DynamicFeature` or Jersey `Factory`: the security filter and the request scoped session binding are
provided by the config.


### 1. Create your own JWT object

You'll need an object that implements [WebSessionPermission](src/main/java/com/coreoz/plume/admin/websession/WebSessionPermission.java).
If you want to use the fingerprint cookie protection, it must also implement
[WebSessionFingerprint](src/main/java/com/coreoz/plume/admin/websession/WebSessionFingerprint.java).
For example:

```java
@Getter
@Setter
@Accessors(chain = true)
public class MyWebSession implements WebSessionPermission, WebSessionFingerprint {

  private Long userId;
  private String userName;
  private Set<String> permissions;
  private String hashedFingerprint;
}
```

If you don't want to use the fingerprint cookie protection, you can simply omit `WebSessionFingerprint`
and configure your session without a fingerprint extractor (see below).


### 2. Declare the security configs in Jersey

Declare your session types with `WebSessionSecurityFeatures.of(...)`, directly in your Jersey configuration:

```java
@Singleton
public class JerseyConfigProvider implements Provider<ResourceConfig> {

    private final ResourceConfig config;

    @Inject
    public JerseyConfigProvider(ObjectMapper objectMapper) {
        config = new ResourceConfig();
        config.packages("com.myapp.webservices");

        // require explicit access control, including your custom annotation
        config.register(RequireExplicitAccessControlFeature.accessControlAnnotations(PublicApi.class, RestrictTo.class));

        // declare the security configs
        config.register(WebSessionSecurityFeatures.of(
            WebSessionSecurityConfig.of(
                MyWebSession.class,
                RestrictTo.class,
                RestrictTo::value,
                MyWebSession::getHashedFingerprint // omit this argument to disable fingerprint verification
            )
        ));
    }

    @Override
    public ResourceConfig get() {
        return config;
    }
}
```

This single registration:
- secures every resource annotated with the access control annotation,
- binds the session object so it can be injected with `@Context MyWebSession`.

> **Why `WebSessionSecurityFeatures.of(...)` and not `config.register(WebSessionSecurityConfig.of(...))`?**
> Jersey only processes a given `Feature` class once. Registering several `WebSessionSecurityConfig`
> instances directly would silently ignore all but the first one. `WebSessionSecurityFeatures` is the
> aggregator that handles all of them through a single Jersey `DynamicFeature` and a single `Binder`.

`WebSessionSecurityFeatures` resolves the `WebSessionSigner` and the security configuration lazily from
the Jersey `InjectionManager` at request time. It can therefore be registered **before** the Guice bridge
(`JerseyGuiceFeature`), with no ordering constraint and no dependency injection needed in
`JerseyConfigProvider`.

You can now secure your webservices using the annotation and the permission string that will be verified
to be in your token:

```java
@RestrictTo(JwtPermission.BASIC_RIGHT)
public class UtilisateurWs {
[...]
}
```

If you prefer to keep using the default administration feature, `config.register(AdminSecurityFeature.class)`
still works exactly as before.


### 3. Getting back the information from the token

With the config-driven feature, the session binding is created for you, so you simply need to add the
object in your webservice method:

```java
@GET
@Path("/me")
public UserBean findMe(@Context MyWebSession securityContext) {
    return userService.findUserDetailByEmail(securityContext.getUserName());
}
```


### 4. Custom login web-service (optional)

If you are using the `plume-admin-ws` module, `SessionWs` handles the default `WebSessionAdmin` password
login and renew workflow.

To reuse it with a custom session type, extend
[PasswordSessionWs](https://github.com/Coreoz/Plume-admin/blob/master/plume-admin-ws/src/main/java/com/coreoz/plume/admin/webservices/session/PasswordSessionWs.java)
and provide a `WebSessionConfig`:

```java
@Path("/client/session")
@Singleton
public class ClientSessionWs extends PasswordSessionWs<ClientWebSession> {

  @Inject
  public ClientSessionWs(AdminUserService adminUserService,
      AdminConfigurationService configurationService,
      AuthSessionTokenHandler authSessionTokenHandler) {
    super(adminUserService, configurationService, authSessionTokenHandler, new ClientWebSessionConfig());
  }

  @PUT
  @Consumes(MediaType.TEXT_PLAIN)
  public Response renew(String webSessionSerialized) {
    return renewSession(webSessionSerialized);
  }
}

class ClientWebSessionConfig implements WebSessionConfig<ClientWebSession> {
  @Override
  public Class<ClientWebSession> sessionClass() {
    return ClientWebSession.class;
  }

  @Override
  public ClientWebSession createSession(AuthenticatedUser user, String hashedFingerprint) {
    return new ClientWebSession()
      .setUserId(user.getUser().getId())
      .setUserName(user.getUser().getUserName())
      .setPermissions(user.getPermissions())
      .setHashedFingerprint(hashedFingerprint);
  }

  @Override
  public long authenticatedUserId(ClientWebSession parsedSession) {
    return parsedSession.getUserId();
  }
}
```

`ClientWebSession` must implement `WebSessionPermission`. If you want the fingerprint to be verified
when the token is read, it must also implement `WebSessionFingerprint` and the security config must
declare a fingerprint extractor (see section 2).

The workflow is built from three layers, all in the `webservices.session` package:

| Class | Role | Endpoints |
|---|---|---|
| `SessionWsSupport<T>` | Session base: `issueSession(AuthenticatedUser)`, `renewSession(String)`. No authentication logic. | none |
| `PasswordSessionWs<T>` | The above + password authentication (credentials, brute-force protection) | `POST` |
| `SessionWs` | The `WebSessionAdmin` administration web-service, which extends `PasswordSessionWs` | `POST`, `PUT renew` |

Only password authentication is implemented today. Because `SessionWsSupport` carries no authentication
logic, another mode can be added by extending it directly: call `issueSession(...)` once the user is
authenticated, and expose `renewSession(...)` on your own endpoint. No password-specific code is
inherited.

**The renewal endpoint is never inherited.** `SessionWsSupport` only provides
`protected renewSession(String)`; each web-service declares its own endpoint, so it can pick its path
and HTTP verb.

**The token representation itself is replaceable.** `AuthSessionTokenHandler` owns everything related
to the token: JWT serialization and signature, expiration, and the fingerprint cookie. It is not
generic — the session type is carried by the `WebSessionConfig` argument — so it is injected directly,
without any `TypeLiteral`, and a single instance serves every session type:

```java
public interface AuthSessionTokenHandler {
  <T extends WebSessionPermission> Response generateUserSession(AuthenticatedUser user, WebSessionConfig<T> config);
  <T extends WebSessionPermission> Optional<T> readUserSession(String webSessionSerialized, WebSessionConfig<T> config);
}
```

The default implementation is `JwtAuthSessionTokenHandler`. Replace it globally with:

```java
bind(AuthSessionTokenHandler.class).to(MyAuthSessionTokenHandler.class);
```

`generateUserSession` is used by both login and renew: a brand new token **and a brand new fingerprint
cookie** are issued on each call. `readUserSession` returns an empty `Optional` when the token is
invalid or expired.


### 5. Multiple session types in the same application

Because each session type is described by its own `WebSessionSecurityConfig`, you can secure different
parts of your application with different sessions. Declare all the configs in a single
`WebSessionSecurityFeatures`:

```java
config.register(WebSessionSecurityFeatures.of(
    WebSessionSecurityConfig.of(
        MyWebSession.class, RestrictTo.class, RestrictTo::value, MyWebSession::getHashedFingerprint),
    WebSessionSecurityConfig.of(
        ClientWebSession.class, RestrictToClient.class, RestrictToClient::value)));
```

Each session type can then be injected independently with `@Context MyWebSession` / `@Context ClientWebSession`.
Remember to list every access control annotation in the `RequireExplicitAccessControlFeature`.


### Fingerprint on a non-secured environment

You might want to disable fingerprint on your development environment in case it's not secured (HTTP and not HTTPS):

```java
admin.session.fingerprint-cookie-https-only = false
admin.session.use-fingerprint-cookie = false
```

Or simply do not provide a fingerprint extractor when creating your `WebSessionSecurityConfig`.
