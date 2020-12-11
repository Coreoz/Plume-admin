package com.coreoz.plume.admin;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class OkHttpLoggerInterceptorFiltersBuilder {
    private static final Logger logger = LoggerFactory.getLogger("api.http");

    private final Set<String> filteredEndPoints;
    private final Set<String> filteredMethods;
    private final Map<String, String> filteredResponseHeaders;

    public OkHttpLoggerInterceptorFiltersBuilder() {
        this.filteredEndPoints = new HashSet<>();
        this.filteredMethods = new HashSet<>();
        this.filteredResponseHeaders = new HashMap<>();
    }

    public OkHttpLoggerInterceptorFiltersBuilder filterEndPoint(String endpoint) {
        this.filteredEndPoints.add(endpoint);
        return this;
    }

    public OkHttpLoggerInterceptorFiltersBuilder filterMethod(HttpMethod method) {
        this.filteredMethods.add(method.name());
        return this;
    }

    public OkHttpLoggerInterceptorFiltersBuilder filterResponseHeaderValue(String header, String value) {
        try {
            this.filteredResponseHeaders.put(header, value);
        } catch (IllegalArgumentException e) {
            return this;
        }
        return this;
    }

    public BiPredicate<Request, Response> build() {
        OkHttpLoggerFilters okHttpLoggerFilters = OkHttpLoggerFilters.of(
            this.filteredEndPoints,
            this.filteredMethods,
            this.filteredResponseHeaders
        );
        return okHttpLoggerFilters.createFilterFunctionFromParameters();
    }
}
