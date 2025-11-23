package org.example.repository.specification.balance;

import org.example.model.CurrencyBalance;
import org.example.repository.specification.AbstractBetweenAmountSpecification;
import org.springframework.stereotype.Component;

@Component
public class AmountBetweenBalanceSpecification extends AbstractBetweenAmountSpecification<CurrencyBalance> {

    private static final String FILTER_KEY = "amountBetween";
    private static final String FIELD_NAME = "amount";

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
