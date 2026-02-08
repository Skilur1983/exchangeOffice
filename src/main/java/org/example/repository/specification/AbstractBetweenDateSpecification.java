package org.example.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

@Slf4j
public abstract class AbstractBetweenDateSpecification<T> implements SpecificationProvider<T> {

    protected abstract String getFieldName();

    @Override
    public abstract String getFilterKey();

    @Override
    public Specification<T> getSpecification(String[] params) {
        if (params == null || params.length == 0 ||
                params[0] == null || params[0].isBlank()) {
            log.debug("Missing or invalid parameters for '{}'. Returning no-op specification.", getFilterKey());
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            LocalDateTime start = parseDateTime(params[0].trim());

            if (params.length == 1 || params[1] == null || params[1].isBlank()) {
                log.debug("Only start date provided for '{}': start='{}'. Filtering >= start.",
                        getFilterKey(), start);
                LocalDateTime finalStart1 = start;
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(root.get(getFieldName()), finalStart1);
            }

            LocalDateTime end = parseDateTime(params[1].trim());

            if (end.isBefore(start)) {
                log.warn("End date is before start date for '{}': start='{}', end='{}'. Swapping values.",
                        getFilterKey(), start, end);
                LocalDateTime temp = start;
                start = end;
                end = temp;
            }

            LocalDateTime finalStart = start;
            LocalDateTime finalEnd = end;

            log.debug("Both start and end provided for '{}': start='{}', end='{}'. Filtering BETWEEN.",
                    getFilterKey(), finalStart, finalEnd);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(getFieldName()), finalStart, finalEnd);

        } catch (DateTimeParseException e) {
            log.warn("Invalid date format for '{}': params={}. Expected yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss",
                    getFilterKey(), Arrays.toString(params));
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
    }

    private LocalDateTime parseDateTime(String dateString) {
        try {
            return LocalDateTime.parse(dateString);
        } catch (DateTimeParseException e) {
            LocalDate date = LocalDate.parse(dateString);
            return date.atStartOfDay();
        }
    }
}

