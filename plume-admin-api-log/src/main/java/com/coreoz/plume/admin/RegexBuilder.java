package com.coreoz.plume.admin;

import java.util.List;

public class RegexBuilder {
    static String computeUrlRegexList(List<String> urlRegexList) {
        return urlRegexList.stream()
            .reduce("", (currentRegex, element) -> {
                if (currentRegex.isEmpty()) {
                    return "(" + element + ")";
                }
                return currentRegex + "|(" + element + ")";
            });
    }
}


