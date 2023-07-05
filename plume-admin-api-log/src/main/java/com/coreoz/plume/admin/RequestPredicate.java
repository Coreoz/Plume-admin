package com.coreoz.plume.admin;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import okhttp3.Request;

/**
 * Represent a predicate of a {@link Request}
 *
 * This is a functional interface</a>
 * whose functional method is {@link #test(Object)}}.
 *
 * The static method {@link #alwaysTrue()} initiate the predicate
 *
 * The default method {@link #filterEndpointStartsWith(String)} filters the request
 * by its endpoint
 *
 * The default method {@link #filterMethod(HttpMethod)} filter the request
 * by its method {@link HttpMethod}
 */
@FunctionalInterface
public interface RequestPredicate extends Predicate<Request> {

    static RequestPredicate alwaysTrue() {
        return request -> true;
    }

    /**
     * Filter endpoint, for example '/api/orders'
     */
    default RequestPredicate filterEndpointStartsWith(String endpointToFilter) {
        return request -> test(request)
            && OkHttpMatchers.matchRequestEndpointStartsWith(request.url(), endpointToFilter);
    }
    default RequestPredicate filterUrlRegex(List<String> urlRegexList) {
        Objects.requireNonNull(urlRegexList);

        if (urlRegexList.isEmpty()) {
            return alwaysTrue();
        }

        Pattern compiledRegex = Pattern.compile(RegexBuilder.computeUrlRegexList(urlRegexList));

        return request -> test(request)
            && OkHttpMatchers.matchRequestUrlRegex(request.url(), compiledRegex);
    }

    default RequestPredicate filterMethod(HttpMethod method) {
        return request -> test(request)
            && OkHttpMatchers.matchRequestMethod(request.method(), method.name());
    }

}
