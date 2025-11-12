package org.example.repository.specification.user;

import lombok.extern.slf4j.Slf4j;
import org.example.model.RoleName;
import org.example.model.User;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoleSpecification implements SpecificationProvider<User> {

    private static final String FILTER_KEY = "role";

    @Override
    public Specification<User> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        try {
            RoleName role = RoleName.valueOf(params[0].trim().toUpperCase());
            return (root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(FILTER_KEY), role);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role value: '{}'. Available roles: {}",
                    params[0], RoleName.values());
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
