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
6. [Generate a JWT secret key](#configuration) and register it in your configuration: `admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"`
7. For non-https environments (i.e. `localhost` for dev), set the configuration value: `admin.session.fingerprint-cookie-https-only = false` (this configuration value should be set to true in HTTPS environments like production)
8. SQL, see [setup files](plume-admin-ws/sql)
9. Install a JS frontend like [Plume Admin UI for React](https://github.com/Coreoz/create-plume-react-project)

Current user access
-------------------
To fetch the current user in an administration web-service,
this Jersey binder must be installed in the Jersey configuration class:
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
To use this module without Admin Web services, you may want to provide implementations of `AdminPermissionService`, `WebSessionSigner`, and `JwtSessionSigner`.
As an example, here is what is defined in the Admin Web-services Guice configuration:
```java
bind(AdminPermissionService.class).to(AdminPermissionServiceBasic.class);
bind(WebSessionSigner.class).toProvider(JwtSessionSignerProvider.class);
bind(JwtSessionSigner.class).toProvider(JwtSessionSignerProvider.class);
```

More documentation about JWT and how to secure project API is available in the [Plume Admin Security module](plume-admin-security).

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
admin.passwords.min-length = 0

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
