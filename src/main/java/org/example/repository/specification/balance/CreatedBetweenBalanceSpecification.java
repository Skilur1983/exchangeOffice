package org.example.repository.specification.balance;

import org.example.model.CurrencyBalance;
import org.example.repository.specification.AbstractBetweenDateSpecification;
import org.springframework.stereotype.Component;

@Component
public class CreatedBetweenBalanceSpecification extends AbstractBetweenDateSpecification<CurrencyBalance> {

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
