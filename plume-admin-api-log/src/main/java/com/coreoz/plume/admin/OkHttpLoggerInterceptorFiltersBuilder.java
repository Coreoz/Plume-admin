package com.coreoz.plume.admin;

import okhttp3.Request;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class OkHttpLoggerInterceptorFiltersBuilder {
    private final Set<String> filteredEndpoints;
    private final Set<String> filteredMethods;

    public OkHttpLoggerInterceptorFiltersBuilder() {
        this.filteredEndpoints = new HashSet<>();
        this.filteredMethods = new HashSet<>();
    }

    public OkHttpLoggerInterceptorFiltersBuilder filterEndpoint(String endpoint) {
        this.filteredEndpoints.add(endpoint);
        return this;
    }

    public OkHttpLoggerInterceptorFiltersBuilder filterMethod(HttpMethod method) {
        this.filteredMethods.add(method.name());
        return this;
    }

    public Predicate<Request> build() {
        return request -> !OkHttpLoggerMatchUtils.matchRequestEndpoints(request.url(), filteredEndpoints)
            && !OkHttpLoggerMatchUtils.matchRequestMethods(request.method(), filteredMethods);
    }
}
