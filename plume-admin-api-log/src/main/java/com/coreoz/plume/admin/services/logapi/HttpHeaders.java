package com.coreoz.plume.admin.services.logapi;


import java.util.List;

import com.coreoz.plume.admin.db.generated.LogHeader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpHeaders {
    private final List<LogHeader> headers;
    private final String mimeType;
}
