package org.example.repository.specification.balance;

import lombok.extern.slf4j.Slf4j;
import org.example.model.CurrencyBalance;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserIdBalanceSpecification implements SpecificationProvider<CurrencyBalance> {

    private static final String FILTER_KEY = "userId";
    private static final String FIELD_NAME = "user";
    private static final String USER_ID_FIELD = "id";

    @Override
    public Specification<CurrencyBalance> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            log.debug("Empty userId filter parameter, returning no filter");
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            Integer userId = Integer.parseInt(params[0].trim());

            log.debug("Creating specification for userId: {}", userId);

            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(FIELD_NAME).get(USER_ID_FIELD), userId);

        } catch (NumberFormatException e) {
            log.warn("Invalid userId format: '{}'. Expected integer value.", params[0]);
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
