package org.example.exchangeOffice.repository.specification;

import org.example.model.Currency;
import org.example.model.CurrencyBalance;
import org.example.repository.CurrencyBalanceRepository;
import org.example.repository.specification.balance.*;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "org.example.repository.specification.balance")
@Sql(
        scripts = {
                "/db/migration/create_tables.sql",
                "/db/migration/insert_fixtures.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class BalanceSpecificationTest {

    @Autowired
    private CurrencyBalanceRepository balanceRepository;

    @Autowired
    private UserIdBalanceSpecification userIdSpec;

    @Autowired
    private CurrencyEnumBalanceSpecification currencySpec;

    @Autowired
    private AmountBetweenBalanceSpecification amountBetweenSpec;

    @Autowired
    private CreatedBetweenBalanceSpecification createdBetweenSpec;

    @Autowired
    private UpdatedBetweenBalanceSpecification updatedBetweenSpec;

    @Test
    void userIdSpec_WhenUserHasMultipleBalances_ReturnsAllBalances() {
        String[] params = {"3"};

        Specification<CurrencyBalance> spec = userIdSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSize(2);
        assertThat(results)
                .allMatch(b -> b.getUser().getId().equals(3))
                .extracting(CurrencyBalance::getCurrency)
                .containsExactlyInAnyOrder(Currency.USD, Currency.EUR);
    }

    @Test
    void userIdSpec_WhenUserHasAllThreeCurrencies_ReturnsThree() {
        String[] params = {"4"};

        Specification<CurrencyBalance> spec = userIdSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(b -> b.getUser().getId().equals(4));
    }

    @Test
    void userIdSpec_WhenUserHasNoBalances_ReturnsEmpty() {
        String[] params = {"2"};

        Specification<CurrencyBalance> spec = userIdSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void userIdSpec_WhenParamsNull_ReturnsAll() {
        String[] params = null;

        Specification<CurrencyBalance> spec = userIdSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void userIdSpec_WhenParamsEmpty_ReturnsAll() {
        String[] params = {};

        Specification<CurrencyBalance> spec = userIdSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void userIdSpec_WhenInvalidUserId_ReturnsEmpty() {
        String[] params = {"9999"};

        Specification<CurrencyBalance> spec = userIdSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void currencySpec_WhenFilteringByUSD_ReturnsOnlyUSDBalances() {
        String[] params = {"USD"};

        Specification<CurrencyBalance> spec = currencySpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b -> b.getCurrency() == Currency.USD);

        assertThat(results)
                .extracting(b -> b.getUser().getId())
                .contains(1, 3, 4, 5, 7, 8, 9); // admin_first, SarSmi, TomBro, etc.
    }

    @Test
    void currencySpec_WhenFilteringByEUR_ReturnsOnlyEURBalances() {
        String[] params = {"EUR"};

        Specification<CurrencyBalance> spec = currencySpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b -> b.getCurrency() == Currency.EUR);
        assertThat(results)
                .extracting(b -> b.getUser().getId())
                .contains(1, 3, 4, 6, 8, 10); // Users with EUR
    }

    @Test
    void currencySpec_WhenFilteringByUAH_ReturnsOnlyUAHBalances() {
        String[] params = {"UAH"};

        Specification<CurrencyBalance> spec = currencySpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b -> b.getCurrency() == Currency.UAH);
        assertThat(results)
                .extracting(b -> b.getUser().getId())
                .contains(1, 4, 7, 8, 10);
    }

    @Test
    void currencySpec_WhenParamsNull_ReturnsAll() {
        String[] params = null;

        Specification<CurrencyBalance> spec = currencySpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void amountBetweenSpec_WhenFilteringSmallAmounts_ReturnsCorrectBalances() {
        String[] params = {"100", "200"};

        Specification<CurrencyBalance> spec = amountBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b ->
                b.getAmount().compareTo(new BigDecimal("100")) >= 0 &&
                        b.getAmount().compareTo(new BigDecimal("200")) <= 0
        );

        assertThat(results)
                .filteredOn(b -> b.getUser().getId().equals(8))
                .hasSize(2);
    }

    @Test
    void amountBetweenSpec_WhenFilteringMediumAmounts_ReturnsCorrectBalances() {
        String[] params = {"1000", "5000"};

        Specification<CurrencyBalance> spec = amountBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b ->
                b.getAmount().compareTo(new BigDecimal("1000")) >= 0 &&
                        b.getAmount().compareTo(new BigDecimal("5000")) <= 0
        );

        assertThat(results).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void amountBetweenSpec_WhenFilteringLargeAmounts_ReturnsCorrectBalances() {
        String[] params = {"10000", "1000000"};

        Specification<CurrencyBalance> spec = amountBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();

        assertThat(results)
                .filteredOn(b -> b.getUser().getId().equals(1))
                .hasSize(2);
    }

    @Test
    void amountBetweenSpec_WhenRangeExcludesAll_ReturnsEmpty() {
        String[] params = {"1", "10"};

        Specification<CurrencyBalance> spec = amountBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void amountBetweenSpec_WhenOnlyMinProvided_FiltersGreaterThanOrEqual() {
        String[] params = {"5000"};

        Specification<CurrencyBalance> spec = amountBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b ->
                b.getAmount().compareTo(new BigDecimal("5000")) >= 0
        );
    }

    @Test
    void amountBetweenSpec_WhenParamsNull_ReturnsAll() {
        String[] params = null;

        Specification<CurrencyBalance> spec = amountBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void createdBetweenSpec_WhenFilteringByToday_ReturnsAllFixtures() {
        String today = java.time.LocalDate.now().toString();
        String[] params = {today};

        Specification<CurrencyBalance> spec = createdBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void createdBetweenSpec_WhenFilteringFutureDates_ReturnsEmpty() {
        String[] params = {"2025-12-01", "2025-12-31"};

        Specification<CurrencyBalance> spec = createdBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void createdBetweenSpec_WhenFilteringPastDates_ReturnsEmpty() {
        String[] params = {"2020-01-01", "2020-12-31"};

        Specification<CurrencyBalance> spec = createdBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void createdBetweenSpec_WhenParamsNull_ReturnsAll() {
        String[] params = null;

        Specification<CurrencyBalance> spec = createdBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void updatedBetweenSpec_WhenFilteringByToday_ReturnsAllFixtures() {
        String today = java.time.LocalDate.now().toString();
        String[] params = {today};

        Specification<CurrencyBalance> spec = updatedBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void updatedBetweenSpec_WhenParamsNull_ReturnsAll() {
        String[] params = null;

        Specification<CurrencyBalance> spec = updatedBetweenSpec.getSpecification(params);
        List<CurrencyBalance> results = balanceRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThan(10);
    }

    @Test
    void combinedSpecs_UserAndCurrency_ReturnsIntersection() {
        Specification<CurrencyBalance> userSpec = userIdSpec.getSpecification(new String[]{"3"});
        Specification<CurrencyBalance> currSpec = currencySpec.getSpecification(new String[]{"USD"});

        Specification<CurrencyBalance> combined = Specification.where(userSpec).and(currSpec);

        List<CurrencyBalance> results = balanceRepository.findAll(combined);

        assertThat(results).hasSize(1);
        CurrencyBalance balance = results.get(0);
        assertThat(balance.getUser().getId()).isEqualTo(3);
        assertThat(balance.getCurrency()).isEqualTo(Currency.USD);
        assertThat(balance.getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    void combinedSpecs_CurrencyAndAmount_ReturnsIntersection() {
        Specification<CurrencyBalance> currSpec = currencySpec.getSpecification(new String[]{"EUR"});
        Specification<CurrencyBalance> amountSpec = amountBetweenSpec.getSpecification(new String[]{"3000", "100000"});

        Specification<CurrencyBalance> combined = Specification.where(currSpec).and(amountSpec);

        List<CurrencyBalance> results = balanceRepository.findAll(combined);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(b ->
                b.getCurrency() == Currency.EUR &&
                        b.getAmount().compareTo(new BigDecimal("3000")) >= 0
        );

        assertThat(results).hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    void combinedSpecs_ThreeFilters_ReturnsCorrectResults() {
        Specification<CurrencyBalance> userSpec = userIdSpec.getSpecification(new String[]{"4"});
        Specification<CurrencyBalance> currSpec = currencySpec.getSpecification(new String[]{"USD"});
        Specification<CurrencyBalance> amountSpec = amountBetweenSpec.getSpecification(new String[]{"1000", "5000"});

        Specification<CurrencyBalance> combined = Specification.where(userSpec)
                .and(currSpec)
                .and(amountSpec);

        List<CurrencyBalance> results = balanceRepository.findAll(combined);

        assertThat(results).hasSize(1);
        CurrencyBalance balance = results.get(0);
        assertThat(balance.getUser().getId()).isEqualTo(4);
        assertThat(balance.getCurrency()).isEqualTo(Currency.USD);
        assertThat(balance.getAmount()).isEqualByComparingTo("2500.00");
    }

    @Test
    void verifyFilterKeys_AllSpecsHaveCorrectKeys() {
        assertThat(userIdSpec.getFilterKey()).isEqualTo("userId");
        assertThat(currencySpec.getFilterKey()).isEqualTo("currency");
        assertThat(amountBetweenSpec.getFilterKey()).isEqualTo("amountBetween");
        assertThat(createdBetweenSpec.getFilterKey()).isEqualTo("created");
        assertThat(updatedBetweenSpec.getFilterKey()).isEqualTo("updated");
    }
}
