Plume Admin
===========

[![Build Status](https://travis-ci.org/Coreoz/Plume-admin.svg?branch=master)](https://travis-ci.org/Coreoz/Plume-admin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin-parent)

Plume Admin is based on [Plume Framework](https://github.com/Coreoz/Plume),
it provides Jersey web services to build an administration area.

If you are looking for a JavaScript frontened that uses these web-services,
check out the [Plume Admin UI for AngularJS](https://github.com/Coreoz/Plume-admin-ui-angularjs).

Looking for a demo? Check out the [Plume Demo project](https://github.com/Coreoz/Plume-demo/tree/master/plume-demo-full-guice-jersey).

Installation
------------
1. Maven dependency: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin)
2. Guice module: `install(new GuiceAdminWithDefaultsModule())`
3. Jersey web-services: `packages("com.coreoz.plume.admin.webservices")`
4. Jersey admin security: `register(AdminSecurityFeature.class)`
5. [Generate a JWT secret key](#configuration) and register it in your configuration: `admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"`
6. SQL, see [setup files](plume-admin-ws/sql)
7. If hibernate is used, install Guice brige module: `install(new GuiceHibernateToQuerydslBridgeModule())`

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
To use this module without Admin Web-services, an implementation of `WebSessionClassProvider` must be provided.

Configuration
-------------
To generate JWT secret, [LastPass generator](https://lastpass.com/generatepassword.php) can be used with a password length of about 50 characters.
```
# this key should be changed in production if test users cannot be trusted
admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"
# default values
admin.session-duration = 12 hours
admin.login.max-attempts = 5
admin.login.blocked-duration = 30 seconds
# enable to ensure that users passwords are long enough
admin.passwords.min-length = 0
```

Upgrade instructions
--------------------
See the [upgrade file](upgrade.md) to see the upgrade instructions.

