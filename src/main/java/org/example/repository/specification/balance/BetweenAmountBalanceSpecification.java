package org.example.repository.specification.balance;

import lombok.extern.slf4j.Slf4j;
import org.example.model.CurrencyBalance;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class BetweenAmountBalanceSpecification implements SpecificationProvider<CurrencyBalance> {

    private static final String FILTER_KEY = "amount";
    private static final String FIELD_NAME = "amount";

    @Override
    public Specification<CurrencyBalance> getSpecification(String[] params) {
        if (params == null || params.length < 2 ||
                params[0] == null || params[0].isBlank() ||
                params[1] == null || params[1].isBlank()) {
            log.debug("Missing or invalid parameters for '{}'. Returning no-op specification.", FILTER_KEY);
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            BigDecimal min = new BigDecimal(params[0].trim());
            BigDecimal max = new BigDecimal(params[1].trim());

            if (max.compareTo(min) < 0) {
                log.warn("Max amount is smaller than min amount for '{}': min='{}', max='{}'. Swapping values.",
                        FILTER_KEY, min, max);
                BigDecimal temp = min;
                min = max;
                max = temp;
            }

            BigDecimal finalMin = min;
            BigDecimal finalMax = max;

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.between(root.get(FIELD_NAME), finalMin, finalMax);

        } catch (NumberFormatException e) {
            log.warn("Invalid number format for '{}': '{}', '{}'. Expected decimal numbers.",
                    FILTER_KEY, params[0], params[1]);
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
