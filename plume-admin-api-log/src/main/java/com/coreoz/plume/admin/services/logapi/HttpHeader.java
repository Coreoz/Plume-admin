package com.coreoz.plume.admin.services.logapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpHeader {
    private final String name;
    private final String value;
}
