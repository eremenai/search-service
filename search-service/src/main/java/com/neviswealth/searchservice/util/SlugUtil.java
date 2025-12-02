package com.neviswealth.searchservice.util;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String slugify(String input) {
        if (input == null) {
            return "";
        }
        input = input.split("\\.")[0];
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }
}
