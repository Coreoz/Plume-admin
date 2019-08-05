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
    private Long id;
    private String api;
    private String url;
    private Instant date;
    private String method;
    private Integer statusCode;
    private String bodyRequest;
    private String bodyResponse;
    private HttpHeaders headerRequest;
    private HttpHeaders headerResponse;
    private Boolean IsCompleteTextRequest;
    private Boolean IsCompleteTextResponse;
}
