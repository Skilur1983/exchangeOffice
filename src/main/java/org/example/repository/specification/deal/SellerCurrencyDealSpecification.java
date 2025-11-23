package org.example.repository.specification.deal;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Currency;
import org.example.model.Deal;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SellerCurrencyDealSpecification implements SpecificationProvider<Deal> {

    private static final String FILTER_KEY = "sellerCurrency";
    private static final String FIELD_NAME = "sellerCurrency";

    @Override
    public Specification<Deal> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            Currency currency = Currency.valueOf(params[0].trim().toUpperCase());

            log.debug("Creating specification for sellerCurrency: {}", currency);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(FIELD_NAME), currency);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid sellerCurrency value: '{}'. Available currencies: {}",
                    params[0], Currency.values());
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
