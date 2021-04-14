package com.coreoz.plume.admin.services.logapi;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LogInterceptApiBean {
    private String url;
    private String method;
    private int statusCode;
    private String bodyRequest;
    private String bodyResponse;
    private List<HttpHeader> headerRequest;
    private List<HttpHeader> headerResponse;
    private String apiName;
}
