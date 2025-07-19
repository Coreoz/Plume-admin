package com.coreoz.plume.admin;

import okhttp3.Headers;
import okhttp3.HttpUrl;

import java.util.Objects;
import java.util.regex.Pattern;

public class OkHttpMatchers {

    public static boolean matchResponseHeaders(Headers headers, String filteredHeaderName, String filteredHeaderValue) {
        Objects.requireNonNull(headers);
        Objects.requireNonNull(filteredHeaderName);
        Objects.requireNonNull(filteredHeaderValue);

        String headerValue = headers.get(filteredHeaderName);
        return headerValue != null && headerValue.equals(filteredHeaderValue);
    }

    public static boolean matchRequestEndpointStartsWith(HttpUrl url, String filteredEndPoint) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(filteredEndPoint);

        return ("/" + String.join("/", url.pathSegments())).startsWith(filteredEndPoint);
    }

    public static boolean matchRequestUrlRegex(HttpUrl url, Pattern compiledRegex) {
        Objects.requireNonNull(url);
        Objects.requireNonNull(compiledRegex);

        return compiledRegex.matcher(url.url().toString()).matches();
    }

    public static boolean matchRequestMethod(String method, String filteredMethod) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(filteredMethod);

        return filteredMethod.equalsIgnoreCase(method);
    }
}
