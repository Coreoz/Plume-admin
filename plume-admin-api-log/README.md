Plume Admin Log API
===================

This module enables to monitor HTTP calls/responses made through [OkHttp](https://square.github.io/okhttp/) or [Retrofit](https://square.github.io/retrofit/).

Installation
------------

1. Maven dependency:
```xml
<dependency>
    <groupId>com.coreoz</groupId>
    <artifactId>plume-admin-ws-system</artifactId>
</dependency>
```
2. Configure the new permission for the admin user to access the API: `LogApiAdminPermissions.MANAGE_API_LOGS`
3. SQL, see [setup files](sql)
4. Configuring the API with OkHttp:
  a. In the API Java class, add a dependency to `LogApiService`
  b. In the OkHttp client creation, add an interceptor to `OkHttpLoggerInterceptor` and choose a name to the API you are using.

For example here is a sample API configuration that would connect to the Github API:
```java
@Inject
public GitHubApi(ObjectMapper objectMapper, LogApiService logApiService) {
    Retrofit retrofit = new Retrofit
        .Builder()
        .client(
            new OkHttpClient.Builder()
                .addInterceptor(new OkHttpLoggerInterceptor("Github", logApiService))
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
        )
        .baseUrl("https://api.github.com")
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build();
    this.gitHubApiService = retrofit.create(GitHubApiService.class);
}
```

Configuration
-------------

Here are the available configuration values with their defaults:
```
api.log.body-max-chars-displayed=1 kB
api.log.cleaning.max-logs-per-api=50
api.log.cleaning.max-duration=14 days
api.log.cleaning.running-every=1 hour
```
