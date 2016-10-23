Plume Admin
===========

Configuration
-------------
To generate JWT secret, [LastPass generator](https://lastpass.com/generatepassword.php) can be used with a password length of about 50 characters.
```
admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"
# default values
admin.session-duration = 12 hours
admin.login.max-attempts = 5
admin.login.blocked-duration = 30 seconds
```

Installation
------------
1. Guice module: `install(new GuiceAdminWithDefaultsModule())`
2. Jersey web-services: `packages("com.coreoz.plume.admin.webservices")`
3. Jersey admin security: `register(AdminSecurityFeature.class)`
4. [Generate a JWT secret key](#configuration) and register it in your configuration: `admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"`5. SQL
5. If hibernate is used, install Guice brige module: `GuiceHibernateToQuerydslBridgeModule`