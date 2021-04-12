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
    private int maxBytes;

    public OkHttpLogEntryTransformerBuilder() {
        this.filteredEndpoints = new HashSet<>();
        this.filteredMethods = new HashSet<>();
        this.filteredResponseHeaders = new HashMap<>();
        this.maxBytes = -1;
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

    public OkHttpLogEntryTransformerBuilder forMaxBodyBytes(int maxBytes) {
        this.maxBytes = maxBytes;
        return this;
    }

    public LogEntryTransformer build() {
        return (request, response, body) -> !OkHttpLoggerMatchUtils.matchRequestEndpoints(request.url(), this.filteredEndpoints)
            && !OkHttpLoggerMatchUtils.matchRequestMethods(request.method(), this.filteredMethods)
            && !OkHttpLoggerMatchUtils.matchResponseHeaders(response, this.filteredResponseHeaders)
            && !(body != null && body.getBytes().length <= maxBytes);
    }
}
