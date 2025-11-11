package org.example.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

@Component
public class FilterParseUtils {

    private FilterParseUtils() {}

    public static Optional<Integer> getInteger(Map<String, String> filters, String key) {
        String value = filters.get(key);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<BigDecimal> getBigDecimal(Map<String, String> filters, String key) {
        String value = filters.get(key);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<LocalDateTime> getLocalDateTime(Map<String, String> filters, String key) {
        String value = filters.get(key);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.parse(value));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static <E extends Enum<E>> Optional<E> getEnum(Map<String, String> filters, String key, Class<E> enumClass) {
        String value = filters.get(key);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Enum.valueOf(enumClass, value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<String> getString(Map<String, String> filters, String key) {
        String value = filters.get(key);
        return (value == null || value.isBlank()) ? Optional.empty() : Optional.of(value);
    }
}
