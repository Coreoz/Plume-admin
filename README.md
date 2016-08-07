Plume Admin
===========

Configuration
-------------
To generate secret, [LastPass generator](https://lastpass.com/generatepassword.php) can be used with a password length of about 50 characters.
```
admin.jwt-secret = "long_generated_password_to_secure_jwt_tokens"
# default values
admin.session-duration = 12 hours
admin.login.max-attempts = 5
admin.login.blocked-duration = 30 seconds
```