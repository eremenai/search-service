package com.neviswealth.searchservice.util;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String slugify(String input) {
        if (input == null) {
            return "";
        }
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}
