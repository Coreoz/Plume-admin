package com.coreoz.plume.admin;

import okhttp3.HttpUrl;
import okhttp3.Response;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class OkHttpLoggerMatchUtils {

    private OkHttpLoggerMatchUtils() {
        // private constructor
    }

    protected static boolean matchResponseHeaders(Response response, Map<String, String> filteredResponseHeaders) {
        Objects.requireNonNull(response);
        return filteredResponseHeaders.entrySet().stream().anyMatch(httpHeadersStringEntry -> {
            String headerValue = response.header(httpHeadersStringEntry.getKey());
            return headerValue != null && headerValue.equals(httpHeadersStringEntry.getValue());
        });
    }

    protected static boolean matchRequestEndpoints(HttpUrl url, Set<String> filteredEndPoints) {
        Objects.requireNonNull(url);
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

    protected static boolean matchRequestMethods(String method, Set<String> filteredMethods) {
        Objects.requireNonNull(method);
        // filteredMethods will always be uppercase since it's added with HttpMethod values
        return filteredMethods.contains(method.toUpperCase());
    }
}
