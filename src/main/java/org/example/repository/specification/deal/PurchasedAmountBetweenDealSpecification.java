package org.example.repository.specification.deal;

import org.example.model.Deal;
import org.example.repository.specification.AbstractBetweenAmountSpecification;
import org.springframework.stereotype.Component;

@Component
public class PurchasedAmountBetweenDealSpecification extends AbstractBetweenAmountSpecification<Deal> {

    private static final String FILTER_KEY = "purchasedAmount";
    private static final String FIELD_NAME = "purchasedAmount";

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

    @Override
    public String getFilterKey() {
        return FILTER_KEY;
    }
}
