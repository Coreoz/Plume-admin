package com.coreoz.plume.admin.services.logApi;

import lombok.Value;

import java.util.List;

@Value(staticConstructor = "of")
public class LogApiFilters {
    List<String> apiNames;
    List<Integer> statusCodes;
}
