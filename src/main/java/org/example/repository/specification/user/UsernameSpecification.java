package org.example.repository.specification.user;

import org.example.model.User;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UsernameSpecification implements SpecificationProvider<User> {

    private static final String FILTER_KEY = "username";

    @Override
    public Specification<User> getSpecification(String[] params) {
        if (params == null || params.length == 0 || params[0] == null || params[0].isBlank()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }

        String username = params[0].trim();

        String escapedUsername = username
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("%", "\\%");

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get(FILTER_KEY)),
                        "%" + escapedUsername.toLowerCase() + "%",
                        '\\'
                );
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}