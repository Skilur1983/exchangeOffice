package org.example.repository.specification.rate;

import org.example.model.DayRate;
import org.example.repository.specification.AbstractBetweenAmountSpecification;
import org.springframework.stereotype.Component;

@Component
public class SellRateBetweenSpecification extends AbstractBetweenAmountSpecification<DayRate> {

    private static final String FILTER_KEY = "sellRate";
    private static final String FIELD_NAME = "sellRate";

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
