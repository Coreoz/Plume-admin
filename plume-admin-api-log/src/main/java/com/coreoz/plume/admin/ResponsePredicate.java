package com.coreoz.plume.admin;

import okhttp3.Response;

import java.util.function.Predicate;

/**
 * Represent a predicate of a {@link Response}
 *
 * This is a functional interface</a>
 * whose functional method is {@link #test(Object)}}.
 *
 * The static method {@link #alwaysTrue()} initiate the predicate
 *
 * The default method {@link #filterHeader(String, String)} filters the response
 * by its headers
 */
@FunctionalInterface
public interface ResponsePredicate extends Predicate<Response> {

    static ResponsePredicate alwaysTrue() {
        return response -> true;
    }

    default ResponsePredicate filterHeader(String headerName, String headerValue) {
        return response -> test(response)
            && OkHttpMatchers.matchResponseHeaders(response.headers(), headerName, headerValue);
    }
}
