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

If you are using the `plume-admin-ws` module, `SessionWs` handles the default `WebSessionAdmin` login and
renew workflow. To reuse this exact workflow with a custom session type, extend
[AbstractSessionWs](https://github.com/Coreoz/Plume-admin/blob/master/plume-admin-ws/src/main/java/com/coreoz/plume/admin/webservices/AbstractSessionWs.java)
and provide a `WebSessionProvider`:

```java
@Path("/client/session")
@Singleton
public class ClientSessionWs extends AbstractSessionWs<ClientWebSession> {

  @Inject
  public ClientSessionWs(AdminUserService adminUserService,
      JwtSessionSigner jwtSessionSigner,
      AdminConfigurationService configurationService,
      AdminSecurityConfigurationService adminSecurityConfigurationService,
      Clock clock) {
    super(
      adminUserService,
      jwtSessionSigner,
      configurationService,
      adminSecurityConfigurationService,
      clock,
      new WebSessionProvider<ClientWebSession>() {
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
      }
    );
  }

  @Override
  protected long authenticatedUserId(ClientWebSession parsedSession) {
    return parsedSession.getUserId();
  }

  @Override
  protected String hashedFingerprint(ClientWebSession parsedSession) {
    return parsedSession.getHashedFingerprint();
  }
}
```


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
