package org.example.utils;

import java.util.Map;

public class PaginationUtils {

    private PaginationUtils() {}

    public static int getPage(Map<String, String> filters, int defaultPage) {
        try {
            return Integer.parseInt(filters.getOrDefault("page", String.valueOf(defaultPage)));
        } catch (NumberFormatException e) {
            return defaultPage;
        }
    }

    public static int getSize(Map<String, String> filters, int defaultSize) {
        try {
            return Integer.parseInt(filters.getOrDefault("size", String.valueOf(defaultSize)));
        } catch (NumberFormatException e) {
            return defaultSize;
        }
    }
}
