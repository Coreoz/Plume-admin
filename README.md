Plume Admin
===========

[![Build Status](https://travis-ci.org/Coreoz/Plume-admin.svg?branch=master)](https://travis-ci.org/Coreoz/Plume-admin)

Installation
------------
1. Maven dependency: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.coreoz/plume-admin)
2. Guice module: `install(new GuiceAdminWithDefaultsModule())`
3. Jersey web-services: `packages("com.coreoz.plume.admin.webservices")`
4. Jersey admin security: `register(AdminSecurityFeature.class)`
5. [Generate a JWT secret key](#configuration) and register it in your configuration: `admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"`
6. SQL
7. If hibernate is used, install Guice brige module: `GuiceHibernateToQuerydslBridgeModule`

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
