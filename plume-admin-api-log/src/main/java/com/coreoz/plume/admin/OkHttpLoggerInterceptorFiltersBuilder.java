package com.coreoz.plume.admin;

import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

    public OkHttpLoggerInterceptorFiltersBuilder addFilteredEndPoint(String endpoint) {
        this.filteredEndPoints.add(endpoint);
        return this;
    }

    public OkHttpLoggerInterceptorFiltersBuilder addFilteredMethod(String method) {
        try {
            HttpMethod methodToAdd = HttpMethod.valueOf(method);
            this.filteredMethods.add(methodToAdd.name());
        } catch (IllegalArgumentException e) {
            return this;
        }
        return this;
    }

    public OkHttpLoggerInterceptorFiltersBuilder addFilteredResponseHeaderValue(String header, String value) {
        try {
            this.filteredResponseHeaders.put(header, value);
        } catch (IllegalArgumentException e) {
            return this;
        }
        return this;
    }

    public BiPredicate<Request, Response> build() {
        OkHttpLoggerFilters okHttpLoggerFilters = new OkHttpLoggerFilters();
        okHttpLoggerFilters.setFilteredEndPoints(this.filteredEndPoints);
        okHttpLoggerFilters.setFilteredResponseHeaders(this.filteredResponseHeaders);
        okHttpLoggerFilters.setFilteredMethods(this.filteredMethods);
        return createFilterFunctionFromParameters(okHttpLoggerFilters);
    }

    public static BiPredicate<Request, Response> createFilterFunctionFromParameters(OkHttpLoggerFilters okHttpLoggerFilters) {
        return (request, response) ->
            shouldRequestMustBeFiltered(request, okHttpLoggerFilters)
                || shouldResponseMustBeFiltered(response, okHttpLoggerFilters);
    }

    private static boolean shouldRequestMustBeFiltered(Request request, OkHttpLoggerFilters okHttpLoggerFilters) {
        if (filterByEndpoint(request, okHttpLoggerFilters.getFilteredEndPoints())) {
            logger.info("Request won't be logged because its endpoint is filtered");
            return true;
        }
        if (filterByMethod(request, okHttpLoggerFilters.getFilteredMethods())) {
            logger.info("Request won't be logged because its method is filtered");
            return true;
        }
        return false;
    }

    private static boolean shouldResponseMustBeFiltered(Response response, OkHttpLoggerFilters okHttpLoggerFilters) {
        if (filterByResponseHeaders(response, okHttpLoggerFilters.getFilteredResponseHeaders())) {
            logger.info("Request won't be logged because one of its header is filtered");
            return true;
        }
        return false;
    }

    private static boolean filterByResponseHeaders(Response response, Map<String, String> filteredResponseHeaders) {
        return filteredResponseHeaders.entrySet().stream().anyMatch(httpHeadersStringEntry -> {
            String headerValue = response.header(httpHeadersStringEntry.getKey());
            return headerValue != null && headerValue.equals(httpHeadersStringEntry.getValue());
        });
    }

    private static boolean filterByEndpoint(Request request, Set<String> filteredEndPoints) {
        return filteredEndPoints.stream().anyMatch(endpoint -> {
            List<String> segments = request.url().pathSegments();
            String[] endpointFilteredSegments = endpoint.split("/");
            if (segments.size() != endpointFilteredSegments.length) {
                return false;
            }
            boolean areAllTheSame = true;
            for (int i = 0; i < endpointFilteredSegments.length; i++) {
                areAllTheSame = areAllTheSame && endpointFilteredSegments[i].equals(segments.get(i));
            }
            return areAllTheSame;
        });
    }

    private static boolean filterByMethod(Request request, Set<String> filteredMethods) {
        return filteredMethods.stream().anyMatch(method ->
            request.method().equalsIgnoreCase(method)
        );
    }
}
