package com.coreoz.plume.admin;


import com.coreoz.plume.admin.services.logapi.LogApiService;
import okhttp3.OkHttpClient;


import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Mutualise les méthodes de création des clients Okhttp et la gestion des retours de Retrofit
 */


@Singleton
public class RetrofitHelperLogApi {

    private final LogApiService logApiService;

    @Inject
    public RetrofitHelperLogApi(LogApiService logApiService) {
        this.logApiService = logApiService;
    }

    public OkHttpClient.Builder debugLoggingHttpClient(String apiName) {
        return new OkHttpClient.Builder()
            .addInterceptor(new OkHttpLoggerInterceptor(apiName, logApiService));
    }

}
