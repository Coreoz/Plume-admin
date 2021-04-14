package com.coreoz.plume.admin;

import okhttp3.Headers;
import okhttp3.HttpUrl;

import java.util.List;
import java.util.Objects;

public class OkHttpMatchers {

    private OkHttpMatchers() {
        // empty constructor
    }

    public static boolean matchResponseHeaders(Headers headers, String filteredHeaderName, String filteredHeaderValue) {
        Objects.requireNonNull(headers);
        Objects.requireNonNull(filteredHeaderName);
        Objects.requireNonNull(filteredHeaderValue);

        String headerValue = headers.get(filteredHeaderName);
        return headerValue != null && headerValue.equals(filteredHeaderValue);
    }

    public static boolean matchRequestEndpoint(HttpUrl url, String filteredEndPoint) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(filteredEndPoint);

        List<String> segments = url.pathSegments();
        // substring : for an endpoint /hello/world, split returns ['', 'hello', 'world'], we must delete the first element
        String[] endpointFilteredSegments = filteredEndPoint.substring(1).split("/");
        if (segments.size() != endpointFilteredSegments.length) {
            return false;
        }
        boolean areAllTheSame = true;
        for (int i = 0; i < endpointFilteredSegments.length; i++) {
            areAllTheSame = areAllTheSame && endpointFilteredSegments[i].equals(segments.get(i));
        }
        return areAllTheSame;
    }

    public static boolean matchRequestMethod(String method, String filteredMethod) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(filteredMethod);

        return filteredMethod.equalsIgnoreCase(method);
    }
}
