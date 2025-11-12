package org.example.repository.specification.user;

import org.example.model.User;
import org.example.repository.specification.AbstractBetweenDateSpecification;
import org.springframework.stereotype.Component;

@Component
public class CreatedBetweenUserSpecification extends AbstractBetweenDateSpecification<User> {

    private static final String FILTER_KEY = "created";
    private static final String FIELD_NAME = "createdAt";

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
