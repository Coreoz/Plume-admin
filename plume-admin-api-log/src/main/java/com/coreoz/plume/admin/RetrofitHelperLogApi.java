package com.coreoz.plume.admin;


import com.coreoz.plume.admin.InterceptorApi;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Mutualise les méthodes de création des clients Okhttp et la gestion des retours de Retrofit
 */

@Singleton
public class RetrofitHelperLogApi {

    private static final Logger logger = LoggerFactory.getLogger(RetrofitHelperLogApi.class);

    private final InterceptorApi httpLoggingInterceptor;

    @Inject
    public RetrofitHelperLogApi(InterceptorApi httpLoggingInterceptor) {
        this.httpLoggingInterceptor = httpLoggingInterceptor;
        this.httpLoggingInterceptor.setLevel(InterceptorApi.Level.BODY);
    }

    public InterceptorApi debugLoggingInterceptor(String apiName) {
        this.httpLoggingInterceptor.setApiName(apiName);
        return httpLoggingInterceptor;
    }


    public OkHttpClient.Builder debugLoggingHttpClient(String apiName) {
        return new OkHttpClient.Builder()
            .addInterceptor(debugLoggingInterceptor(apiName));
    }

    public static <T> T executeNonEmptyRequest(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }

            throw new RuntimeException(
                "Error during HTTP call on "
                    + call.request()
                    + " : "
                    + response.code() + " " + response.message()
                    + " - "
                    + response.errorBody().string()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
