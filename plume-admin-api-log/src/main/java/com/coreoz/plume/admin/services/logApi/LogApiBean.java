package com.coreoz.plume.admin.services.logApi;



import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LogApiBean {
    @JsonSerialize(using = ToStringSerializer.class)
    private final Long id;
    private final String api;
    private final String url;
    private final Instant date;
    private final String method;
    private final Integer statusCode;
    private final String bodyRequest;
    private final String bodyResponse;
    private final HttpHeaders headerRequest;
    private final HttpHeaders headerResponse;
    private final Boolean isCompleteTextRequest;
    private final Boolean isCompleteTextResponse;
}
