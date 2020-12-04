package com.coreoz.plume.admin;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
class OkHttpLoggerFilters {
    private Set<String> filteredEndPoints;
    private Set<String> filteredMethods;
    private Map<String, String> filteredResponseHeaders;
}
