package org.example.config;

import org.example.model.CurrencyBalance;
import org.example.model.DayRate;
import org.example.model.Deal;
import org.example.model.User;
import org.example.repository.specification.SpecificationManager;
import org.example.repository.specification.SpecificationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpecificationConfig {

    @Bean
    public SpecificationManager<User> userSpecificationManager(
            List<SpecificationProvider<User>> providers) {
        return new SpecificationManager<>(providers);
    }

    @Bean
    public SpecificationManager<Deal> dealSpecificationManager(
            List<SpecificationProvider<Deal>> providers) {
        return new SpecificationManager<>(providers);
    }

    @Bean
    public SpecificationManager<DayRate> dayRateSpecificationManager(
            List<SpecificationProvider<DayRate>> providers) {
        return new SpecificationManager<>(providers);
    }

    @Bean
    public SpecificationManager<CurrencyBalance> currencyBalanceSpecificationManager(
            List<SpecificationProvider<CurrencyBalance>> providers) {
        return new SpecificationManager<>(providers);
    }
}
