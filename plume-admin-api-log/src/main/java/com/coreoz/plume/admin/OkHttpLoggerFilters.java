package com.coreoz.plume.admin;

import lombok.Value;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

@Value(staticConstructor = "of")
public class OkHttpLoggerFilters {
    Set<String> filteredEndPoints;
    Set<String> filteredMethods;
    Map<String, String> filteredResponseHeaders;

    public BiPredicate<Request, Response> createFilterFunctionFromParameters() {
        return (request, response) ->
            matchRequestEndpoints(request, this.filteredEndPoints)
            || matchRequestMethods(request, this.filteredMethods)
            || matchResponseHeaders(response, this.filteredResponseHeaders);
    }

    private static boolean matchResponseHeaders(Response response, Map<String, String> filteredResponseHeaders) {
        return filteredResponseHeaders.entrySet().stream().anyMatch(httpHeadersStringEntry -> {
            String headerValue = response.header(httpHeadersStringEntry.getKey());
            return headerValue != null && headerValue.equals(httpHeadersStringEntry.getValue());
        });
    }

    private static boolean matchRequestEndpoints(Request request, Set<String> filteredEndPoints) {
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

    private static boolean matchRequestMethods(Request request, Set<String> filteredMethods) {
        return filteredMethods.stream().anyMatch(method ->
            request.method().equalsIgnoreCase(method)
        );
    }
}
