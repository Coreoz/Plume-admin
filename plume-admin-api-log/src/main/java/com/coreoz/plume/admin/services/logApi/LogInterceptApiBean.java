package com.coreoz.plume.admin.services.logApi;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogInterceptApiBean {
    private final String url;
    private final String method;
    private final int statusCode;
    private final String bodyRequest;
    private final String bodyResponse;
    private final List<HttpHeader> headerRequest;
    private final List<HttpHeader> headerResponse;
    private final String apiName;
}
