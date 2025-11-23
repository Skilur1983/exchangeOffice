package org.example.repository.specification.deal;

import lombok.extern.slf4j.Slf4j;
import org.example.model.Deal;
import org.example.model.DealType;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DealTypeSpecification implements SpecificationProvider<Deal> {

    private static final String FILTER_KEY = "dealType";
    private static final String FIELD_NAME = "dealType";

    @Override
    public Specification<Deal> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            DealType dealType = DealType.valueOf(params[0].trim().toUpperCase());

            log.debug("Creating specification for dealType: {}", dealType);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(FIELD_NAME), dealType);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid dealType value: '{}'. Available types: {}",
                    params[0], DealType.values());
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
