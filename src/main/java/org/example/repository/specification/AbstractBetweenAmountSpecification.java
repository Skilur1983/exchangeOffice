package org.example.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;

@Slf4j
public abstract class AbstractBetweenAmountSpecification<T> implements SpecificationProvider<T> {

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
            BigDecimal min = new BigDecimal(params[0].trim());

            if (params.length == 1 || params[1] == null || params[1].isBlank()) {
                log.debug("Only min provided for '{}': min='{}'. Filtering >= min.", getFilterKey(), min);
                BigDecimal finalMin1 = min;
                return (root, query, criteriaBuilder) ->
                        criteriaBuilder.greaterThanOrEqualTo(root.get(getFieldName()), finalMin1);
            }

            BigDecimal max = new BigDecimal(params[1].trim());

            if (max.compareTo(min) < 0) {
                log.warn("Max is smaller than min for '{}': min='{}', max='{}'. Swapping values.",
                        getFilterKey(), min, max);
                BigDecimal temp = min;
                min = max;
                max = temp;
            }

            BigDecimal finalMin = min;
            BigDecimal finalMax = max;

            log.debug("Both min and max provided for '{}': min='{}', max='{}'. Filtering BETWEEN.",
                    getFilterKey(), finalMin, finalMax);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(getFieldName()), finalMin, finalMax);

        } catch (NumberFormatException e) {
            log.warn("Invalid number format for '{}': params={}. Expected decimal numbers.",
                    getFilterKey(), Arrays.toString(params));
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
    }
}
