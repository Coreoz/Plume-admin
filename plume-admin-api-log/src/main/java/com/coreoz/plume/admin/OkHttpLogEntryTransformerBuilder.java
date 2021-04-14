package com.coreoz.plume.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OkHttpLogEntryTransformerBuilder {
    private final Set<String> filteredEndpoints;
    private final Set<String> filteredMethods;
    private final Map<String, String> filteredResponseHeaders;
    private boolean forAll;
    private int maxChar;

    public OkHttpLogEntryTransformerBuilder() {
        this.filteredEndpoints = new HashSet<>();
        this.filteredMethods = new HashSet<>();
        this.filteredResponseHeaders = new HashMap<>();
        this.forAll = false;
        this.maxChar = -1;
    }

    public OkHttpLogEntryTransformerBuilder forEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint);
        this.filteredEndpoints.add(endpoint);
        return this;
    }

    public OkHttpLogEntryTransformerBuilder forMethod(HttpMethod method) {
        Objects.requireNonNull(method);
        this.filteredMethods.add(method.name());
        return this;
    }

    public OkHttpLogEntryTransformerBuilder forResponseHeaderValue(String header, String value) {
        Objects.requireNonNull(header);
        Objects.requireNonNull(value);
        this.filteredResponseHeaders.put(header, value);
        return this;
    }

    public OkHttpLogEntryTransformerBuilder forAll() {
        this.forAll = true;
        return this;
    }

    public OkHttpLogEntryTransformerBuilder forMaxBodyChar(int maxChar) {
        this.maxChar = maxChar;
        return this;
    }

    /**
     * This method builds a {@link LogEntryTransformer} from arguments
     * {@link #forEndpoint(String)} : add an endpoint to a list to be filtered (ex: /hello/world)
     * {@link #forMethod(HttpMethod)} : add an {@link HttpMethod} to a list to be filtered (ex: POST)
     * {@link #forResponseHeaderValue(String, String)} : add an header with a value to a map to be filtered (ex: <"Content-Type", "application/pdf">)
     * {@link #forAll()} : add a limit if defined for all traces
     * {@link #forMaxBodyChar(int)} : set a limit for traces
     *
     * @return a {@link LogEntryTransformer} that transforms a {@link com.coreoz.plume.admin.services.logapi.LogInterceptApiBean} accordingly
     */
    public LogEntryTransformer build() {
        return (request, response, logInterceptApiBean) -> {
            boolean isNotTransformable = !forAll
                && !OkHttpMatchers.matchRequestEndpoints(request.url(), this.filteredEndpoints)
                && !OkHttpMatchers.matchRequestMethods(request.method(), this.filteredMethods)
                && !OkHttpMatchers.matchResponseHeaders(response, this.filteredResponseHeaders);

            return isNotTransformable
                ? logInterceptApiBean
                : LogEntryTransformer.limitBodySizeTransformer(this.maxChar).transform(request, response, logInterceptApiBean);
        };
    }
}
