package com.coreoz.plume.admin;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import okhttp3.Request;

/**
 * Represents a predicate of a {@link Request}
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #test(Object)}}.
 *
 * <p>The static method {@link #alwaysTrue()} initiates the predicate
 *
 */
@FunctionalInterface
public interface RequestPredicate extends Predicate<Request> {

    static RequestPredicate alwaysTrue() {
        return request -> true;
    }

    /**
     * Filters a request by its endpoint when starting with the argument
     * @param endpointToFilter the endpoint to filter
     */
    default RequestPredicate filterEndpointStartsWith(String endpointToFilter) {
        return request -> test(request)
            && OkHttpMatchers.matchRequestEndpointStartsWith(request.url(), endpointToFilter);
    }

    /**
     * Filters a request by its URL through a URL regex list
     * @param urlRegexList : the URL regex list to be filtered
     */
    default RequestPredicate filterUrlRegex(List<String> urlRegexList) {
        Objects.requireNonNull(urlRegexList);

        if (urlRegexList.isEmpty()) {
            return alwaysTrue();
        }

        Pattern compiledRegex = Pattern.compile(RegexBuilder.computeUrlRegexList(urlRegexList));

        return request -> test(request)
            && OkHttpMatchers.matchRequestUrlRegex(request.url(), compiledRegex);
    }

    /**
     * Filters a request by its {@link HttpMethod} when matching the argument
     * @param method the {@link HttpMethod} to filter
     */
    default RequestPredicate filterMethod(HttpMethod method) {
        return request -> test(request)
            && OkHttpMatchers.matchRequestMethod(request.method(), method.name());
    }

}
