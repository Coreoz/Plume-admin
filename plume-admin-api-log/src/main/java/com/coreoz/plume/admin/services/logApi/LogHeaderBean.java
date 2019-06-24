package com.coreoz.plume.admin.services.logApi;


import com.coreoz.plume.admin.db.generated.LogHeader;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class LogHeaderBean {
    private List<LogHeader> header;
    private String mode;
}
