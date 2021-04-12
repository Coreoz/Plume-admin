package com.coreoz.plume.admin;

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public class OkHttpLoggerInterceptorFilters {

    private OkHttpLoggerInterceptorFilters() {
        // private constructor
    }

    public static Predicate<Request> createRequestFilterFunctionFromParameters(
        Set<String> filteredEndpoints,
        Set<String> filteredMethods
    ) {
        return request -> !matchRequestEndpoints(request.url(), filteredEndpoints)
            && !matchRequestMethods(request.method(), filteredMethods);
    }

    public static Predicate<Response> createResponseFilterFunctionFromParameters(
        Set<String> filteredEndpoints,
        Set<String> filteredMethods,
        Map<String, String> filteredResponseHeaders
    ) {
        return response -> !matchRequestEndpoints(response.request().url(), filteredEndpoints)
            && !matchRequestMethods(response.request().method(), filteredMethods)
            && !matchResponseHeaders(response, filteredResponseHeaders);
    }

    private static boolean matchResponseHeaders(Response response, Map<String, String> filteredResponseHeaders) {
        return filteredResponseHeaders.entrySet().stream().anyMatch(httpHeadersStringEntry -> {
            String headerValue = response.header(httpHeadersStringEntry.getKey());
            return headerValue != null && headerValue.equals(httpHeadersStringEntry.getValue());
        });
    }

    private static boolean matchRequestEndpoints(HttpUrl url, Set<String> filteredEndPoints) {
        return filteredEndPoints.stream().anyMatch(endpoint -> {
            List<String> segments = url.pathSegments();
            // substring : for an endpoint /hello/world, split returns ['', 'hello', 'world'], we must delete the first element
            String[] endpointFilteredSegments = endpoint.substring(1).split("/");
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

    private static boolean matchRequestMethods(String method, Set<String> filteredMethods) {
        // will always bu uppercase since it's added with HttpMethod values
        return filteredMethods.contains(method.toUpperCase());
    }
}
