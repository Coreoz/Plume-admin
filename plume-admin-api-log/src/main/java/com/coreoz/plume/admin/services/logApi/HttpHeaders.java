package com.coreoz.plume.admin.services.logApi;


import java.util.List;

import com.coreoz.plume.admin.db.generated.LogHeader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpHeaders {
    private List<LogHeader> headers;
    private String mimeType;
}
