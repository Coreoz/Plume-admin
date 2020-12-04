package com.coreoz.plume.admin;

import com.sun.research.ws.wadl.HTTPMethods;
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
            HTTPMethods methodToAdd = HTTPMethods.fromValue(method);
            this.filteredMethods.add(methodToAdd.value());
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
        return (request, response) ->
            this.shouldRequestMustBeFiltered(request)
                || this.shouldResponseMustBeFiltered(response);
    }

    private boolean shouldRequestMustBeFiltered(Request request) {
        if (this.filterByEndpoint(request)) {
            logger.info("Request won't be logged because its endpoint is filtered");
            return true;
        }
        if (this.filterByMethod(request)) {
            logger.info("Request won't be logged because its method is filtered");
            return true;
        }
        return false;
    }

    private boolean shouldResponseMustBeFiltered(Response response) {
        if (this.filterByResponseHeaders(response)) {
            logger.info("Request won't be logged because one of its header is filtered");
            return true;
        }
        return false;
    }

    private boolean filterByResponseHeaders(Response response) {
        return this.filteredResponseHeaders.entrySet().stream().anyMatch(httpHeadersStringEntry -> {
            String headerValue = response.header(httpHeadersStringEntry.getKey());
            return headerValue != null && headerValue.equals(httpHeadersStringEntry.getValue());
        });
    }

    private boolean filterByEndpoint(Request request) {
        return this.filteredEndPoints.stream().anyMatch(endpoint -> {
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

    private boolean filterByMethod(Request request) {
        return this.filteredMethods.stream().anyMatch(method ->
            request.method().equalsIgnoreCase(method)
        );
    }
}
