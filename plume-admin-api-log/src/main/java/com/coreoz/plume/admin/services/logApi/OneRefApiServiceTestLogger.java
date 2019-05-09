package com.coreoz.plume.admin.services.logApi;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

import java.util.List;




public interface OneRefApiServiceTestLogger {

    @GET("one-referential-msv/countries")
    Call<List<CountryBeanTest>> countries(
        @Header("User-Id") String userId,
        @Header("External-Application") String externalApplication
    );
}
