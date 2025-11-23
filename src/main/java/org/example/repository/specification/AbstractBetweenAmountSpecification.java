package org.example.repository.specification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

@Slf4j
public abstract class AbstractBetweenAmountSpecification<T> implements SpecificationProvider<T> {

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
            BigDecimal min = new BigDecimal(params[0].trim());
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

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(getFieldName()), finalMin, finalMax);

        } catch (NumberFormatException e) {
            log.warn("Invalid number format for '{}': '{}', '{}'. Expected decimal numbers.",
                    getFilterKey(), params[0], params[1]);
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
    }
}
