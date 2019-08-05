package com.coreoz.plume.admin.services.logApi;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogInterceptApiBean {
    private String url;
    private String method;
    private String statusCode;
    private String bodyRequest;
    private String bodyResponse;
    private List<HttpHeader> headerRequest;
    private List<HttpHeader> headerResponse;
    private String apiName;
}
