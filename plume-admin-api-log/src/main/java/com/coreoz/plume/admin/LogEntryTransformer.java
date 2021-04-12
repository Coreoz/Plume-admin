package com.coreoz.plume.admin;

import okhttp3.Request;
import okhttp3.Response;

@FunctionalInterface
public interface LogEntryTransformer {
    boolean log(Request request, Response response, String body);

    default LogEntryTransformer negate() {
        return (Request request, Response response, String body) -> !log(request, response, body);
    }
}
