package org.example.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Slf4j
public abstract class AbstractBetweenDateSpecification<T> implements SpecificationProvider<T> {

    protected abstract String getFieldName();

    @Override
    public abstract String getFilterKey();

    @Override
    public Specification<T> getSpecification(String[] params) {
        if (params == null || params.length < 2 ||
                params[0] == null || params[0].isBlank() ||
                params[1] == null || params[1].isBlank()) {
            log.debug("Missing or invalid parameters for '{}'. Returning no-op specification.", getFilterKey());
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            LocalDateTime start = LocalDateTime.parse(params[0].trim());
            LocalDateTime end = LocalDateTime.parse(params[1].trim());

            if (end.isBefore(start)) {
                log.warn("End date is before start date for '{}': start='{}', end='{}'. Swapping values.",
                        getFilterKey(), start, end);
                LocalDateTime temp = start;
                start = end;
                end = temp;
            }

            LocalDateTime finalStart = start;
            LocalDateTime finalEnd = end;

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(getFieldName()), finalStart, finalEnd);

        } catch (DateTimeParseException e) {
            log.warn("Invalid date format for '{}': '{}', '{}'. Expected ISO format (yyyy-MM-ddTHH:mm:ss)",
                    getFilterKey(), params[0], params[1]);
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
    }
}

