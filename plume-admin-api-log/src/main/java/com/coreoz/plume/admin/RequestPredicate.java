package com.coreoz.plume.admin;

import okhttp3.Request;

import java.util.function.Predicate;

/**
 * Represent a predicate of a {@link Request}
 *
 * This is a functional interface</a>
 * whose functional method is {@link #test(Object)}}.
 *
 * The static method {@link #alwaysTrue()} initiate the predicate
 *
 * The default method {@link #filterEndpoint(String)} filters the request
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

    default RequestPredicate filterEndpoint(String endpointToFilter) {
        return request -> test(request)
            && OkHttpMatchers.matchRequestEndpointStartsWith(request.url(), endpointToFilter);
    }

    default RequestPredicate filterMethod(HttpMethod method) {
        return request -> test(request)
            && OkHttpMatchers.matchRequestMethod(request.method(), method.name());
    }

}
