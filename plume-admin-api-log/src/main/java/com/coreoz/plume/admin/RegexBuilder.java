package com.coreoz.plume.admin;

import java.util.List;

public class RegexBuilder {
    private RegexBuilder() {
        // Empty constructor
    }

    static String regexHidingFields(List<String> keysToHide) {
        String regexKeys = keysToHide.stream()
            .reduce("", (currentRegex, element) -> {
                if (!currentRegex.isEmpty()) {
                    return currentRegex + "|(?<=\"" + element + "\":\")";
                }
                return "(?<=\"" + element + "\":\")";
            });

        if (!regexKeys.isEmpty()) {
            return "(" + regexKeys + ").*?(?=\")";
        }

        return "";
    }

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
