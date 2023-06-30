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

Filter & transform logs
-----------------------

Filtering and transforming the logs is possible using the `OkHttpLoggerInterceptor`:
- `OkHttpLoggerInterceptor(String apiName, LogApiService logApiService, RequestPredicate requestFilterPredicate)`: to only filter requests
- `OkHttpLoggerInterceptor(String apiName, LogApiService logApiService, LogEntryTransformer logEntryTransformer)`: to transform logs and to filter requests/responses

Example to omit logging requests that start with `/api/orders`:
```java
new OkHttpLoggerInterceptor(
  "Github",
  logApiService,
  RequestPredicate.alwaysTrue().filterEndpointStartsWith("/api/orders")
)
```

Example to hide certain json objet keys. Here : `contractId` and `password` values will be replace by `****` :

*Only work key/value and not array or objects*
```java
new OkHttpLoggerInterceptor(
  "Github",
  logApiService,
  LogEntryTransformer.emptyTransformer()
    .hideJsonFields(
        "((?<=\"contractId\":\")|(?<=\"password\":\")).*?(?=\")",
        "****"
    )
)
```

Example to log only the first 1024 chars of the request/response body:
```java
new OkHttpLoggerInterceptor(
  "Github",
  logApiService,
  LogEntryTransformer.limitBodySizeTransformer(1024)
)
```

Example to log only the first 1024 chars of the request/response body and to omit logging requests that start with `/api/orders`:
```java
new OkHttpLoggerInterceptor(
  "Github",
  logApiService,
  LogEntryTransformer
    .limitBodySizeTransformer(1024)
    .applyOnlyToRequests(RequestPredicate.alwaysTrue().filterEndpointStartsWith("/api/orders"))
)
```

`LogEntryTransformer` and `RequestPredicate` are interfaces, so the possibilities of transforming and filtering are prettry much endless.

Configuration
-------------

Here are the available configuration values with their defaults:
```
api.log.body-max-chars-displayed=1 kB
api.log.cleaning.max-logs-per-api=50
api.log.cleaning.max-duration=14 days
api.log.cleaning.running-every=1 hour
```
