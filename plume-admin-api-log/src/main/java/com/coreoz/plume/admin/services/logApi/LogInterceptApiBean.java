package com.coreoz.plume.admin.services.logApi;




import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@AllArgsConstructor
@Getter
@Value(staticConstructor = "of")
public class LogInterceptApiBean {
    private String url;
    private String method;
    private String statusCode;
    private String bodyRequest;
    private String bodyResponse;
    private List<LogInterceptHeaderBean> headerRequest;
    private List<LogInterceptHeaderBean> headerResponse;
    private String apiName;
}
