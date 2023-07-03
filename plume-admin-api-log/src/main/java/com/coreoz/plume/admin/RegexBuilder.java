package com.coreoz.plume.admin;

import java.util.List;

public class RegexBuilder {

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
}
