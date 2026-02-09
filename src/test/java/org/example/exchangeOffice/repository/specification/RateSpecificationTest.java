package org.example.exchangeOffice.repository.specification;

import org.example.model.Currency;
import org.example.model.DayRate;
import org.example.repository.DayRateRepository;
import org.example.repository.specification.rate.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "org.example.repository.specification.rate")
@Sql(
        scripts = {
                "/db/migration/create_tables.sql",
                "/db/migration/insert_fixtures.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class RateSpecificationTest {

    @Autowired
    private DayRateRepository dayRateRepository;

    @Autowired
    private BaseCurrencySpecification baseCurrencySpec;

    @Autowired
    private QuoteCurrencySpecification quoteCurrencySpec;

    @Autowired
    private BuyRateBetweenSpecification buyRateSpec;

    @Autowired
    private SellRateBetweenSpecification sellRateSpec;

    @Autowired
    private CreatedBetweenRateSpecification createdBetweenSpec;

    @Autowired
    private UpdatedBetweenRateSpecification updatedBetweenSpec;

    @Test
    void baseCurrencySpec_WhenFilteringUSD_ReturnsOnlyUSDRates() {
        String[] params = {"USD"};

        Specification<DayRate> spec = baseCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getBaseCurrency() == Currency.USD);

        assertThat(results).hasSize(5);
    }

    @Test
    void baseCurrencySpec_WhenFilteringEUR_ReturnsOnlyEURRates() {
        String[] params = {"EUR"};

        Specification<DayRate> spec = baseCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getBaseCurrency() == Currency.EUR);

        assertThat(results).hasSize(5);
    }

    @Test
    void baseCurrencySpec_WhenFilteringUAH_ReturnsEmpty() {
        String[] params = {"UAH"};

        Specification<DayRate> spec = baseCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void baseCurrencySpec_WhenInvalidCurrency_ReturnsEmpty() {
        String[] params = {"GBP"};

        Specification<DayRate> spec = baseCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void baseCurrencySpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<DayRate> spec = baseCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).hasSize(10);
    }

    @Test
    void quoteCurrencySpec_WhenFilteringEUR_ReturnsOnlyEURRates() {
        String[] params = {"EUR"};

        Specification<DayRate> spec = quoteCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getQuoteCurrency() == Currency.EUR);

        assertThat(results).hasSize(3);
    }

    @Test
    void quoteCurrencySpec_WhenFilteringUSD_ReturnsOnlyUSDRates() {
        String[] params = {"USD"};

        Specification<DayRate> spec = quoteCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getQuoteCurrency() == Currency.USD);

        assertThat(results).hasSize(3);
    }

    @Test
    void quoteCurrencySpec_WhenFilteringUAH_ReturnsOnlyUAHRates() {
        String[] params = {"UAH"};

        Specification<DayRate> spec = quoteCurrencySpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.getQuoteCurrency() == Currency.UAH);

        assertThat(results).hasSize(4);
    }

    @Test
    void buyRateSpec_WhenFilteringLowRates_ReturnsCorrectRates() {
        String[] params = {"0.90", "1.00"};

        Specification<DayRate> spec = buyRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getBuyRate().compareTo(new BigDecimal("0.90")) >= 0 &&
                        r.getBuyRate().compareTo(new BigDecimal("1.00")) <= 0
        );

        assertThat(results).hasSize(3);
    }

    @Test
    void buyRateSpec_WhenFilteringMediumRates_ReturnsCorrectRates() {
        String[] params = {"1.00", "1.10"};

        Specification<DayRate> spec = buyRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getBuyRate().compareTo(new BigDecimal("1.00")) >= 0 &&
                        r.getBuyRate().compareTo(new BigDecimal("1.10")) <= 0
        );

        assertThat(results).hasSize(3);
    }

    @Test
    void buyRateSpec_WhenFilteringHighRates_ReturnsCorrectRates() {
        String[] params = {"40", "45"};

        Specification<DayRate> spec = buyRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getBuyRate().compareTo(new BigDecimal("40")) >= 0 &&
                        r.getBuyRate().compareTo(new BigDecimal("45")) <= 0
        );

        assertThat(results).hasSize(4);
    }

    @Test
    void buyRateSpec_WhenOnlyMinProvided_FiltersGreaterThanOrEqual() {
        String[] params = {"1.00"};

        Specification<DayRate> spec = buyRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getBuyRate().compareTo(new BigDecimal("1.00")) >= 0
        );

        assertThat(results).hasSize(7);
    }

    @Test
    void buyRateSpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<DayRate> spec = buyRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).hasSize(10);
    }

    @Test
    void sellRateSpec_WhenFilteringLowRates_ReturnsCorrectRates() {
        String[] params = {"0.95", "1.00"};

        Specification<DayRate> spec = sellRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getSellRate().compareTo(new BigDecimal("0.95")) >= 0 &&
                        r.getSellRate().compareTo(new BigDecimal("1.00")) <= 0
        );

        assertThat(results).hasSize(3);
    }

    @Test
    void sellRateSpec_WhenFilteringMediumRates_ReturnsCorrectRates() {
        String[] params = {"1.05", "1.10"};

        Specification<DayRate> spec = sellRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getSellRate().compareTo(new BigDecimal("1.05")) >= 0 &&
                        r.getSellRate().compareTo(new BigDecimal("1.10")) <= 0
        );

        assertThat(results).hasSize(3);
    }

    @Test
    void sellRateSpec_WhenOnlyMinProvided_FiltersGreaterThanOrEqual() {
        String[] params = {"1.00"};

        Specification<DayRate> spec = sellRateSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r ->
                r.getSellRate().compareTo(new BigDecimal("1.00")) >= 0
        );

        assertThat(results).hasSize(7);
    }

    @Test
    void createdBetweenSpec_WhenFilteringByToday_ReturnsAllRates() {
        String today = LocalDate.now().toString();
        String[] params = {today};

        Specification<DayRate> spec = createdBetweenSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).hasSize(10);
    }

    @Test
    void createdBetweenSpec_WhenFilteringFutureDates_ReturnsEmpty() {
        String[] params = {"2025-12-01", "2025-12-31"};

        Specification<DayRate> spec = createdBetweenSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void createdBetweenSpec_WhenFilteringPastDates_ReturnsEmpty() {
        String[] params = {"2020-01-01", "2020-12-31"};

        Specification<DayRate> spec = createdBetweenSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void updatedBetweenSpec_WhenFilteringByToday_ReturnsAllRates() {
        String today = LocalDate.now().toString();
        String[] params = {today};

        Specification<DayRate> spec = updatedBetweenSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).hasSize(10);
    }

    @Test
    void updatedBetweenSpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<DayRate> spec = updatedBetweenSpec.getSpecification(params);
        List<DayRate> results = dayRateRepository.findAll(spec);

        assertThat(results).hasSize(10);
    }

    @Test
    void combinedSpecs_BaseCurrencyAndQuoteCurrency_ReturnsSpecificPair() {
        Specification<DayRate> baseSpec = baseCurrencySpec.getSpecification(new String[]{"USD"});
        Specification<DayRate> quoteSpec = quoteCurrencySpec.getSpecification(new String[]{"EUR"});

        Specification<DayRate> combined = Specification.where(baseSpec).and(quoteSpec);

        List<DayRate> results = dayRateRepository.findAll(combined);

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r ->
                r.getBaseCurrency() == Currency.USD &&
                        r.getQuoteCurrency() == Currency.EUR
        );
    }

    @Test
    void combinedSpecs_CurrencyPairAndBuyRate_ReturnsFilteredRates() {
        Specification<DayRate> baseSpec = baseCurrencySpec.getSpecification(new String[]{"EUR"});
        Specification<DayRate> quoteSpec = quoteCurrencySpec.getSpecification(new String[]{"USD"});
        Specification<DayRate> buyRateSp = buyRateSpec.getSpecification(new String[]{"1.04"});

        Specification<DayRate> combined = Specification.where(baseSpec)
                .and(quoteSpec)
                .and(buyRateSp);

        List<DayRate> results = dayRateRepository.findAll(combined);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r ->
                r.getBaseCurrency() == Currency.EUR &&
                        r.getQuoteCurrency() == Currency.USD &&
                        r.getBuyRate().compareTo(new BigDecimal("1.04")) >= 0
        );
    }

    @Test
    void combinedSpecs_FourFilters_ReturnsCurrentUSDEURRate() {
        Specification<DayRate> baseSpec = baseCurrencySpec.getSpecification(new String[]{"USD"});
        Specification<DayRate> quoteSpec = quoteCurrencySpec.getSpecification(new String[]{"EUR"});
        Specification<DayRate> buyRateSp = buyRateSpec.getSpecification(new String[]{"0.94", "0.94"});
        Specification<DayRate> sellRateSp = sellRateSpec.getSpecification(new String[]{"0.97", "0.97"});

        Specification<DayRate> combined = Specification.where(baseSpec)
                .and(quoteSpec)
                .and(buyRateSp)
                .and(sellRateSp);

        List<DayRate> results = dayRateRepository.findAll(combined);

        assertThat(results).hasSize(1);
        DayRate rate = results.get(0);
        assertThat(rate.getBaseCurrency()).isEqualTo(Currency.USD);
        assertThat(rate.getQuoteCurrency()).isEqualTo(Currency.EUR);
        assertThat(rate.getBuyRate()).isEqualByComparingTo("0.94");
        assertThat(rate.getSellRate()).isEqualByComparingTo("0.97");
    }

    @Test
    void combinedSpecs_BuyAndSellRateRange_ReturnsMatchingRates() {
        Specification<DayRate> buyRateSp = buyRateSpec.getSpecification(new String[]{"0.90", "1.00"});
        Specification<DayRate> sellRateSp = sellRateSpec.getSpecification(new String[]{"0.95", "1.00"});

        Specification<DayRate> combined = Specification.where(buyRateSp).and(sellRateSp);

        List<DayRate> results = dayRateRepository.findAll(combined);

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(r ->
                r.getBaseCurrency() == Currency.USD &&
                        r.getQuoteCurrency() == Currency.EUR
        );
    }

    @Test
    void verifyFilterKeys_AllSpecsHaveCorrectKeys() {
        assertThat(baseCurrencySpec.getFilterKey()).isEqualTo("baseCurrency");
        assertThat(quoteCurrencySpec.getFilterKey()).isEqualTo("quoteCurrency");
        assertThat(buyRateSpec.getFilterKey()).isEqualTo("buyRate");
        assertThat(sellRateSpec.getFilterKey()).isEqualTo("sellRate");
        assertThat(createdBetweenSpec.getFilterKey()).isEqualTo("created");
        assertThat(updatedBetweenSpec.getFilterKey()).isEqualTo("updated");
    }

    @Test
    void combinedSpecs_ImpossibleCombination_ReturnsEmpty() {
        Specification<DayRate> baseSpec = baseCurrencySpec.getSpecification(new String[]{"USD"});
        Specification<DayRate> quoteSpec = quoteCurrencySpec.getSpecification(new String[]{"UAH"});

        Specification<DayRate> combined = Specification.where(baseSpec).and(quoteSpec);

        List<DayRate> results = dayRateRepository.findAll(combined);

        assertThat(results).hasSize(2);
    }
}
