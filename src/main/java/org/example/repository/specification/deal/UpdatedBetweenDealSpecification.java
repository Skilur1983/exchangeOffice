package org.example.repository.specification.deal;

import org.example.model.Deal;
import org.example.repository.specification.AbstractBetweenDateSpecification;
import org.springframework.stereotype.Component;

@Component
public class UpdatedBetweenDealSpecification extends AbstractBetweenDateSpecification<Deal> {

    private static final String FILTER_KEY = "updated";
    private static final String FIELD_NAME = "updatedAt";

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
