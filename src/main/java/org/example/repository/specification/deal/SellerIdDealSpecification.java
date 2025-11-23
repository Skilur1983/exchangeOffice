package org.example.repository.specification.deal;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Deal;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SellerIdDealSpecification implements SpecificationProvider<Deal> {

    private static final String FILTER_KEY = "sellerId";

    @Override
    public Specification<Deal> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            Integer sellerId = Integer.parseInt(params[0].trim());

            log.debug("Creating specification for sellerId: {}", sellerId);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("seller").get("id"), sellerId);

        } catch (NumberFormatException e) {
            log.warn("Invalid sellerId format: '{}'. Expected integer value.", params[0]);
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
