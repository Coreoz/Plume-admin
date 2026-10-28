Plume Admin
===========

[![Build Status](https://travis-ci.org/Coreoz/Plume-admin.svg?branch=master)](https://travis-ci.org/Coreoz/Plume-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin-parent)

Plume Admin is based on [Plume Framework](https://github.com/Coreoz/Plume),
it provides Jersey web services to build an administration area.

If you are looking for a JavaScript frontend that uses these web-services,
check out the [Plume Admin UI for React](https://github.com/Coreoz/create-plume-react-project).

Looking for a demo? Check out the [Plume Demo project](https://github.com/Coreoz/Plume-showcase).

Installation
------------
1. Maven dependency:
```xml
<dependency>
    <groupId>com.coreoz</groupId>
    <artifactId>plume-admin-ws</artifactId>
</dependency>
```
2. Guice module: `install(new GuiceAdminWsWithDefaultsModule())`
3. Jersey web-services: `packages("com.coreoz.plume.admin.webservices")`
4. Jersey admin security: `register(AdminSecurityFeature.class)`
5. Jersey security: If the access control mechanism is set up, you need to add the `RestrictToAdmin.class` access control annotation: `config.register(RequireExplicitAccessControlFeature.accessControlAnnotations(PublicApi.class, RestrictToAdmin.class));`
   - For custom session types or multiple sessions, declare the security configs in your Jersey configuration: `config.register(WebSessionSecurityFeatures.of(WebSessionSecurityConfig.of(...)))` (see [custom session documentation](plume-admin-security#creating-my-own-permission-workflow-using-plume))
6. [Generate a JWT secret key](#configuration) and register it in your configuration: `admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"`
7. For non-https environments (i.e. `localhost` for dev), set the configuration value: `admin.session.fingerprint-cookie-https-only = false` (this configuration value should be set to true in HTTPS environments like production)
8. SQL, see [setup files](plume-admin-ws/sql)
9. Install a JS frontend like [Plume Admin UI for React](https://github.com/Coreoz/create-plume-react-project)

Current user access
-------------------
To fetch the current user in an administration web-service, inject the session object with `@Context`:
```java
@GET
public UserBean findMe(@Context WebSessionAdmin webSessionAdmin) { ... }
```

Since **5.2**, the session binding is registered automatically when you declare a `WebSessionSecurityConfig`
(see [custom session documentation](plume-admin-security#creating-my-own-permission-workflow-using-plume)):
```java
config.register(WebSessionSecurityFeatures.of(WebSessionSecurityConfig.admin()));
```

The manual Jersey binder below is only needed when you do **not** use `WebSessionSecurityFeatures`
(for example when you only register `AdminSecurityFeature`). Do not mix both, otherwise the
`WebSessionAdmin` binding would be registered twice:
```java
register(new AbstractBinder() {
	@Override
	protected void configure() {
		bindFactory(WebSessionAdminFactory.class).to(WebSessionPermission.class).in(RequestScoped.class);
		bindFactory(WebSessionAdminFactory.class).to(WebSessionAdmin.class).in(RequestScoped.class);
	}
});
```

Admin security
--------------
### Replaceable session components

| Component | Default | Responsibility | How to override |
|---|---|---|---|
| `WebSessionSecurityConfig` | `WebSessionSecurityConfig.admin()` | Describes a session type for Jersey: session class, access control annotation, fingerprint extractor | Declare your own with `WebSessionSecurityConfig.of(...)` |
| `WebSessionSigner` | `JwtSessionSigner` | Serializes/reads the token (signature, expiration) | `bind(WebSessionSigner.class).to(MySigner.class)` |
| `AuthSessionTokenHandler` | `JwtAuthSessionTokenHandler` | Issues the complete login/renew response (token + fingerprint cookie) and reads it back | `bind(AuthSessionTokenHandler.class).to(MyHandler.class)` |
| `WebSessionConfig<T>` | `AdminWebSessionConfig` | Creates a session object for a given user and extracts its user id | Pass your own config to your web-service (not a Guice binding) |
| `AdminPermissionService` | `AdminPermissionServiceBasic` | Checks the permissions of the current session | `bind(AdminPermissionService.class).to(...)` |

The Guice bindings applied by `GuiceAdminWsWithDefaultsModule`:
```java
bind(AdminPermissionService.class).to(AdminPermissionServiceBasic.class);
bind(WebSessionSigner.class).toProvider(JwtSessionSignerProvider.class);
bind(JwtSessionSigner.class).toProvider(JwtSessionSignerProvider.class);
bind(AuthSessionTokenHandler.class).to(JwtAuthSessionTokenHandler.class);
```

`AuthSessionTokenHandler` groups all the token logic: JWT serialization and signature, expiration, and
the optional fingerprint cookie. It is not generic (the session type is passed as a `WebSessionConfig`
argument), so it is bound and injected directly, without any `TypeLiteral`. A single instance serves
every session type.

### Web-services

The session workflow is built from three layers, all in the `webservices.session` package:

| Class | Role | Endpoints |
|---|---|---|
| `SessionWsSupport<T>` | Session base: issues a token from an authenticated user, renews a token. Contains **no authentication logic**. | none |
| `PasswordSessionWs<T>` | Adds password authentication: credentials validation and brute-force protection | `@POST authenticate` |
| `SessionWs` | The default administration web-service, for the `WebSessionAdmin` session type | `@POST authenticate` (inherited), `@PUT renew` |

Only password authentication is implemented today. Because `SessionWsSupport` carries no
authentication logic, another mode can be added by extending it directly: call `issueSession(...)` once
the user is authenticated, and expose `renewSession(...)` on your own endpoint. Extend
`PasswordSessionWs` to keep password authentication with a custom session type.

The renew endpoint is never inherited: each web-service declares its own endpoint from
`renewSession(...)`, so it can choose its path and HTTP verb.

See the [Plume Admin Security module](plume-admin-security#4-custom-login-web-service-optional) for a
complete example.

### Securing APIs

Since **5.2**, security is declared in Jersey instead of writing a custom `SecurityFeature`:
each session type is described by a `WebSessionSecurityConfig`, and the configs are registered through
`WebSessionSecurityFeatures` which secures the APIs annotated with the access control annotation and
binds the session object for `@Context` injection:
```java
config.register(RequireExplicitAccessControlFeature.accessControlAnnotations(PublicApi.class, RestrictToAdmin.class));
config.register(WebSessionSecurityFeatures.of(WebSessionSecurityConfig.admin()));
```

More documentation about JWT, custom sessions and custom login web-services is available in the
[Plume Admin Security module](plume-admin-security).

Configuration
-------------
To generate JWT secret, [LastPass generator](https://lastpass.com/generatepassword.php) can be used with a password length of about 50 characters.
```
# this key should be changed in production if test users cannot be trusted
admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"

# default values
# the duration after which a session token expires
admin.session.expire-duration = 1 minute
# the duration after which the client should refresh the session token (must be lower than the expire duration)
admin.session.refresh-duration = 20 seconds
# the duration after which the client should stop refreshing the session token (must be greater than the expire duration)  
admin.session.inactive-duration = 15 minutes
admin.login.max-attempts = 5
admin.login.blocked-duration = 30 seconds

# Login timing protector max measurments that are kept to generate random delays when username is not found during login
admin.login.timing-protector.max-samples = 10
# Login timing protector measurements rate that are kept after `max-samples` measurments are reached
admin.login.timing-protector.sampling-rate = 0.22

# if a secure cookie is emitted alongside the JWT token to prevent XSS attacks
# see https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet_for_Java.html for details
admin.session.use-fingerprint-cookie = true
# on localhost when using HTTP, this option must be set to false => this should be set to true at least on production
admin.session.fingerprint-cookie-https-only = true

# enable to ensure that users passwords are long enough
admin.passwords.min-length = 0
```

WS System module
----------------
To set up the module, install the Plume Schedule module in `ApplicationModule`: `install(new GuiceSchedulerModule());`

Password hashing
----------------
Plume Admin already handles passwords hashing with BCrypt. It is used in the `plm_user` table.

However, you can rely on the code provided in Plume Admin to implement user authentication and password hashing in your own database tables. To do that, you will want to implement an `HashService`. One is already provided:

```java
bind(HashService.class).to(BCryptHashService.class);
```

Note that this service is already bound if you are already using `GuiceAdminWsModule` or `GuiceAdminWsWithDefaultsModule`; 

You'll use it to hash the password:

```java
userDB.setPassword(hashService.hashPassword(userBean.getPassword()));
```

and to check if the provided password match the one registered:

```java
if (hashService.checkPassword(loginBean.getPassword(), userDB.getPassword())) {
  // Password is correct
}
```

Protection against user enumeration through login timing attacks
----------------------------------------------------------------
Plume Admin authentication relies on the library [Login Time Attack Protector](https://github.com/Coreoz/Login-time-attack-protector) to protect against user enumeration through login timing attacks.

HTTP API Log module
-------------------
To set up the module:
- Maven:
```xml
<dependency>
  <groupId>com.coreoz</groupId>
  <artifactId>plume-admin-api-log</artifactId>
</dependency>
```
- Install the Plume Schedule module in `ApplicationModule`: `install(new GuiceSchedulerModule());`
- Scheduler:
```java
LogApiScheduledJobs logApiScheduledJobs; // from dependency injection
logApiScheduledJobs.scheduleJobs();
```
- [See SQL files](plume-admin-api-log/sql/)

Advanced configuration is detailed in the [Log API module](plume-admin-api-log/#filter--transform-logs).

Upgrade instructions
--------------------
See the [releases notes](https://github.com/Coreoz/Plume-admin/releases) to see the upgrade instructions.
