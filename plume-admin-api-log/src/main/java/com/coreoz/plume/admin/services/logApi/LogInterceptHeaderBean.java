package com.coreoz.plume.admin.services.logApi;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@AllArgsConstructor
@Getter
@Value(staticConstructor = "of")
public class LogInterceptHeaderBean {
    private String key;
    private String value;
}
