package org.example.repository.specification.deal;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Deal;
import org.example.model.DealStatus;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DealStatusSpecification implements SpecificationProvider<Deal> {

    private static final String FILTER_KEY = "status";
    private static final String FIELD_NAME = "status";

    @Override
    public Specification<Deal> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            DealStatus status = DealStatus.valueOf(params[0].trim().toUpperCase());

            log.debug("Creating specification for status: {}", status);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(FIELD_NAME), status);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid status value: '{}'. Available statuses: {}",
                    params[0], DealStatus.values());
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
