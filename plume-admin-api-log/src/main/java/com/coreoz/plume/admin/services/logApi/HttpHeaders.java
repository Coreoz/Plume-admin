package com.coreoz.plume.admin.services.logApi;


import java.util.List;

import com.coreoz.plume.admin.db.generated.LogHeader;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpHeaders {
    private List<LogHeader> header;
    // TODO should be replaced by the mime type
    private String mode;
}
