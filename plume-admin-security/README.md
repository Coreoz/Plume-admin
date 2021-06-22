# Plume Admin Security

## Plume Admin general use

For general use, please check the [main Plume Admin documentation](https://github.com/Coreoz/Plume-admin)


## Creating my own permission workflow using Plume

For general use [WebSessionAdmin](src/main/java/com/coreoz/plume/admin/websession/WebSessionAdmin.java) is provided, but you might want to add other fields in your JWT token.
This guide describe how to handle your own JWT token based session and how to secure your API: this way API endpoints you configure will require a valid JWT session in order to be accessed.


### 1. Create your own JWT object

You'll need an object that implement both [WebSessionPermission](src/main/java/com/coreoz/plume/admin/websession/WebSessionPermission.java) and [WebSessionFingerprint](src/main/java/com/coreoz/plume/admin/websession/WebSessionFingerprint.java).
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

If you don't need session permissions and/or fingerprints, you'll need to implement your own [RequestPermissionProvider](src/main/java/com/coreoz/plume/admin/websession/jersey/WebSessionRequestPermissionProvider.java),
and specify that you web session won't extend WebSessionPermission and/or WebSessionFingerprint.


### 2. Create the bean that will be return by your login webservice

This object will contains at least your jwt token:

```java
public class LoginBean {
  @JsonSerialize(using = ToStringSerializer.class)
  private String token;

  public LoginBean(String token) {
    this.token = token;
  } 
}
```


### 3. Generate your return bean using your token

For this step, you should have bind the JWT Signer and have a JWT secret like detailed in the [main documentation](https://github.com/Coreoz/Plume-admin).
You may want to add a time provider to specify a token duration.

```java
bind(TimeProvider.class).to(SystemTimeProvider.class);
```

Converting your data to the token object:

```java
private MyWebSession convertToJWTSession(User user) {
    return new MyWebSession().setUserId(user.getId()).setUserName(user.getEmail()).setPermissions(user.getPermissions());
}
```

And your token object to your jwt token string

```java
String jwtToken = webSessionSigner.serializeSession(
                convertToJWTSession(user),
                timeProvider.currentTime() + TOKEN_EXPIRY_DURATION.toMillis()
            );
```


### 4. Add security to my webservices by adding a filter on jwt permission in Jersey

By default you might be using [AdminSecurityFeature](src/main/java/com/coreoz/plume/admin/guice/jersey/feature/AdminSecurityFeature.java), but this is tied to both [WebSessionAdmin](src/main/java/com/coreoz/plume/admin/websession/WebSessionAdmin.java) and [RestrictToAdmin](src/main/java/com/coreoz/plume/admin/guice/jersey/feature/RestrictToAdmin.java)

You'll need to add a class that implements `DynamicFeature`. You need to indicate what is the expected object when the token is parsed and to what annotation is security will be linked to.

In our example we will use `RestrictTo` as the annotation, then we would have:

```java 
@Singleton
public class SecurityFeature implements DynamicFeature {

    private final PermissionFeature<RestrictTo> permissionFeature;

    @Inject
    public SecurityFeature(WebSessionSigner webSessionSigner, AdminSecurityConfigurationService configurationService) {
        this.permissionFeature = new PermissionFeature<>(
            new WebSessionRequestPermissionProvider<>(
                webSessionSigner,
                MyWebSession.class,
                configurationService.sessionUseFingerprintCookie()
            ),
            RestrictTo.class,
            RestrictTo::value
        );
    }

    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        permissionFeature.configure(resourceInfo, context);
    }
}
```

In your Jersey config file make sure the annotation is registered and add your new `SecurityFeature`
You'll need to add any custom Annotation.

```java
config.register(RequireExplicitAccessControlFeature.accessControlAnnotations(RestrictTo.class, PublicApi.class));
config.register(SecurityFeature.class);
```

You can now secure your webservices using the `RestrictTo` annotation and the permission string that will be verified to be in your token:

```java
@RestrictTo(JwtPermission.BASIC_RIGHT)
public class UtilisateurWs {
[...]
}
```

Final step is to provide a `Factory` that will be used to create back your `MyWebSession` object from the received request. Below is the corresponding example:

```java
public class MyWebSessionFactory implements Factory<MyWebSession> {

    private final ContainerRequestContext context;
    private final WebSessionSigner webSessionSigner;
    private final boolean verifyCookieFingerprint;
  
    @Inject
    public MyWebSessionFactory(ContainerRequestContext context, WebSessionSigner sessionSigner,
        AdminSecurityConfigurationService configurationService) {
      this.context = context;
      this.webSessionSigner = sessionSigner;
      this.verifyCookieFingerprint = configurationService.sessionUseFingerprintCookie();
    }
  
    @Override
    public MyWebSession provide() {
      return JerseySessionParser.currentSessionInformation(context, webSessionSigner, MyWebSession.class, verifyCookieFingerprint);
    }
  
    @Override
    public void dispose(JwtPermission permission) {
      // unused
    }
  
}
```

and register it in Jersey:

```java
config.register(new AbstractBinder() {
      @Override
      protected void configure() {
        bindFactory(MyWebSessionFactory.class).to(WebSessionPermission.class).in(RequestScoped.class);
        bindFactory(MyWebSessionFactory.class).to(MyWebSession.class).in(RequestScoped.class);
      }
    });
```


You might want to disable fingerprint on your development environnement in case it's not secured (HTTP and not HTTPS)

```java 
admin.session.fingerprint-cookie-https-only = false
admin.session.use-fingerprint-cookie = false
```


### 5. Getting back the information from the token

You simply need to add the object in your webservice method:

```java
@GET
    @Path("/me")
    public UserBean findMe(@Context MyWebSession securityContext) {
        return userService.findUserDetailByEmail(securityContext.getUserName());
    }
```


