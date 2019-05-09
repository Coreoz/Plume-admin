package com.coreoz.plume.admin.services.logApi;

import com.coreoz.plume.admin.RetrofitHelperLogApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import retrofit2.converter.jackson.JacksonConverterFactory;
@Singleton
public class OneRefApiTestLogger {

    private final OneRefApiServiceTestLogger oneRefApiService;

    @Inject
    public OneRefApiTestLogger(ObjectMapper objectMapper, RetrofitHelperLogApi retrofitHelperLogApi) {

        Retrofit retrofit = new Retrofit
            .Builder()
            .client(retrofitHelperLogApi.debugLoggingHttpClient("OneRefTEST").build())
            .baseUrl("https://emagin-dms-api.int.coreoz.com/")
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build();
        this.oneRefApiService = retrofit.create(OneRefApiServiceTestLogger.class);
    }


    public List<CountryBeanTest> countries() throws IOException {
        return executeListRequest(oneRefApiService.countries(
            "wlf-admin-user",
            "wlf-admin"
        ));
    }

    private<T> T executeRequest(Call<T> call) throws IOException {
        Response<T> response = call.execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body();
        }

        return null;
    }

    private<T> List<T> executeListRequest(Call<List<T>> call) throws IOException {
        return MoreObjects.firstNonNull(executeRequest(call), ImmutableList.of());
    }

}
