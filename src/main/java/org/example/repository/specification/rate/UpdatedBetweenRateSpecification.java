package org.example.repository.specification.rate;

import org.example.model.DayRate;
import org.example.repository.specification.AbstractBetweenDateSpecification;
import org.springframework.stereotype.Component;

@Component
public class UpdatedBetweenRateSpecification extends AbstractBetweenDateSpecification<DayRate> {

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
