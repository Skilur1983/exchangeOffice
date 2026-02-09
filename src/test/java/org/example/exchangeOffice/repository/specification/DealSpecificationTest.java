package org.example.exchangeOffice.repository.specification;

import org.example.model.*;
import org.example.repository.DealRepository;
import org.example.repository.specification.deal.*;
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
@ComponentScan(basePackages = "org.example.repository.specification.deal")
@Sql(
        scripts = {
                "/db/migration/create_tables.sql",
                "/db/migration/insert_fixtures.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class DealSpecificationTest {

    @Autowired
    private DealRepository dealRepository;

    @Autowired
    private UserIdDealSpecification userIdSpec;

    @Autowired
    private SellerIdDealSpecification sellerIdSpec;

    @Autowired
    private BuyerIdDealSpecification buyerIdSpec;

    @Autowired
    private DealTypeSpecification dealTypeSpec;

    @Autowired
    private DealStatusSpecification statusSpec;

    @Autowired
    private SellerCurrencyDealSpecification sellerCurrencySpec;

    @Autowired
    private BuyerCurrencyDealSpecification buyerCurrencySpec;

    @Autowired
    private SoldAmountBetweenDealSpecification soldAmountSpec;

    @Autowired
    private PurchasedAmountBetweenDealSpecification purchasedAmountSpec;

    @Autowired
    private ExchangeRateBetweenDealSpecification exchangeRateSpec;

    @Autowired
    private CreatedBetweenDealSpecification createdBetweenSpec;

    @Autowired
    private UpdatedBetweenDealSpecification updatedBetweenSpec;

    @Test
    void userIdSpec_WhenUserIsSellerInMultipleDeals_ReturnsAllDeals() {
        String[] params = {"1"};

        Specification<Deal> spec = userIdSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getSeller().getId().equals(1) || d.getBuyer().getId().equals(1)
        );
    }

    @Test
    void userIdSpec_WhenUserIsBuyerInDeals_ReturnsAllDeals() {
        String[] params = {"3"};

        Specification<Deal> spec = userIdSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getSeller().getId().equals(3) || d.getBuyer().getId().equals(3)
        );
    }

    @Test
    void userIdSpec_WhenUserNotInAnyDeals_ReturnsEmpty() {
        String[] params = {"2"};

        Specification<Deal> spec = userIdSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void sellerIdSpec_WhenSellerHasDeals_ReturnsOnlyThoseDeals() {
        String[] params = {"1"};

        Specification<Deal> spec = sellerIdSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getSeller().getId().equals(1));
    }

    @Test
    void sellerIdSpec_WhenSellerHasNoDeals_ReturnsEmpty() {
        String[] params = {"2"};

        Specification<Deal> spec = sellerIdSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void buyerIdSpec_WhenBuyerHasDeals_ReturnsOnlyThoseDeals() {
        String[] params = {"1"};

        Specification<Deal> spec = buyerIdSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getBuyer().getId().equals(1));
    }

    @Test
    void dealTypeSpec_WhenFilteringBuyDeals_ReturnsOnlyBuyDeals() {
        String[] params = {"BUY"};

        Specification<Deal> spec = dealTypeSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getDealType() == DealType.BUY);
    }

    @Test
    void dealTypeSpec_WhenFilteringSellDeals_ReturnsOnlySellDeals() {
        String[] params = {"SELL"};

        Specification<Deal> spec = dealTypeSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getDealType() == DealType.SELL);
    }

    @Test
    void dealTypeSpec_WhenInvalidType_ReturnsEmpty() {
        String[] params = {"INVALID"};

        Specification<Deal> spec = dealTypeSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void statusSpec_WhenFilteringCompleted_ReturnsOnlyCompleted() {
        String[] params = {"COMPLETED"};

        Specification<Deal> spec = statusSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getStatus() == DealStatus.COMPLETED);
    }

    @Test
    void statusSpec_WhenFilteringPaused_ReturnsOnlyPaused() {
        String[] params = {"PAUSED"};

        Specification<Deal> spec = statusSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getStatus() == DealStatus.PAUSED);
    }

    @Test
    void statusSpec_WhenFilteringCancelled_ReturnsOnlyCancelled() {
        String[] params = {"CANCELLED"};

        Specification<Deal> spec = statusSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getStatus() == DealStatus.CANCELLED);
    }

    @Test
    void sellerCurrencySpec_WhenFilteringUSD_ReturnsOnlyUSDSellers() {
        String[] params = {"USD"};

        Specification<Deal> spec = sellerCurrencySpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getSellerCurrency() == Currency.USD);
    }

    @Test
    void sellerCurrencySpec_WhenFilteringEUR_ReturnsOnlyEURSellers() {
        String[] params = {"EUR"};

        Specification<Deal> spec = sellerCurrencySpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getSellerCurrency() == Currency.EUR);
    }

    @Test
    void buyerCurrencySpec_WhenFilteringEUR_ReturnsOnlyEURBuyers() {
        String[] params = {"EUR"};

        Specification<Deal> spec = buyerCurrencySpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getBuyerCurrency() == Currency.EUR);
    }

    @Test
    void buyerCurrencySpec_WhenFilteringUAH_ReturnsOnlyUAHBuyers() {
        String[] params = {"UAH"};

        Specification<Deal> spec = buyerCurrencySpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d -> d.getBuyerCurrency() == Currency.UAH);
    }

    @Test
    void soldAmountSpec_WhenFilteringSmallAmounts_ReturnsCorrectDeals() {
        String[] params = {"100", "1000"};

        Specification<Deal> spec = soldAmountSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getSoldAmount().compareTo(new BigDecimal("100")) >= 0 &&
                        d.getSoldAmount().compareTo(new BigDecimal("1000")) <= 0
        );
    }

    @Test
    void soldAmountSpec_WhenOnlyMinProvided_FiltersGreaterThanOrEqual() {
        String[] params = {"1000"};

        Specification<Deal> spec = soldAmountSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getSoldAmount().compareTo(new BigDecimal("1000")) >= 0
        );
    }

    @Test
    void purchasedAmountSpec_WhenFilteringRange_ReturnsCorrectDeals() {
        String[] params = {"500", "2000"};

        Specification<Deal> spec = purchasedAmountSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getPurchasedAmount().compareTo(new BigDecimal("500")) >= 0 &&
                        d.getPurchasedAmount().compareTo(new BigDecimal("2000")) <= 0
        );
    }

    @Test
    void exchangeRateSpec_WhenFilteringByRate_ReturnsCorrectDeals() {
        String[] params = {"0.90", "1.10"};

        Specification<Deal> spec = exchangeRateSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getExchangeRateUsed().compareTo(new BigDecimal("0.90")) >= 0 &&
                        d.getExchangeRateUsed().compareTo(new BigDecimal("1.10")) <= 0
        );
    }

    @Test
    void createdBetweenSpec_WhenFilteringByToday_ReturnsAllDeals() {
        String today = java.time.LocalDate.now().toString();
        String[] params = {today};

        Specification<Deal> spec = createdBetweenSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isNotEmpty();
    }

    @Test
    void createdBetweenSpec_WhenFilteringFutureDates_ReturnsEmpty() {
        String[] params = {"2025-12-01", "2025-12-31"};

        Specification<Deal> spec = createdBetweenSpec.getSpecification(params);
        List<Deal> results = dealRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void combinedSpecs_UserAndStatus_ReturnsIntersection() {
        Specification<Deal> userSpec = userIdSpec.getSpecification(new String[]{"3"});
        Specification<Deal> statusSp = statusSpec.getSpecification(new String[]{"COMPLETED"});

        Specification<Deal> combined = Specification.where(userSpec).and(statusSp);

        List<Deal> results = dealRepository.findAll(combined);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                (d.getSeller().getId().equals(3) || d.getBuyer().getId().equals(3)) &&
                        d.getStatus() == DealStatus.COMPLETED
        );
    }

    @Test
    void combinedSpecs_TypeAndCurrency_ReturnsIntersection() {
        Specification<Deal> typeSpec = dealTypeSpec.getSpecification(new String[]{"SELL"});
        Specification<Deal> currencySpec = sellerCurrencySpec.getSpecification(new String[]{"USD"});

        Specification<Deal> combined = Specification.where(typeSpec).and(currencySpec);

        List<Deal> results = dealRepository.findAll(combined);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getDealType() == DealType.SELL &&
                        d.getSellerCurrency() == Currency.USD
        );
    }

    @Test
    void combinedSpecs_FourFilters_ReturnsCorrectResults() {
        Specification<Deal> typeSpec = dealTypeSpec.getSpecification(new String[]{"SELL"});
        Specification<Deal> statusSp = statusSpec.getSpecification(new String[]{"COMPLETED"});
        Specification<Deal> sellerSpec = sellerIdSpec.getSpecification(new String[]{"1"});
        Specification<Deal> currencySpec = sellerCurrencySpec.getSpecification(new String[]{"USD"});

        Specification<Deal> combined = Specification.where(typeSpec)
                .and(statusSp)
                .and(sellerSpec)
                .and(currencySpec);

        List<Deal> results = dealRepository.findAll(combined);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(d ->
                d.getDealType() == DealType.SELL &&
                        d.getStatus() == DealStatus.COMPLETED &&
                        d.getSeller().getId().equals(1) &&
                        d.getSellerCurrency() == Currency.USD
        );
    }

    @Test
    void verifyFilterKeys_AllSpecsHaveCorrectKeys() {
        assertThat(userIdSpec.getFilterKey()).isEqualTo("userId");
        assertThat(sellerIdSpec.getFilterKey()).isEqualTo("sellerId");
        assertThat(buyerIdSpec.getFilterKey()).isEqualTo("buyerId");
        assertThat(dealTypeSpec.getFilterKey()).isEqualTo("dealType");
        assertThat(statusSpec.getFilterKey()).isEqualTo("status");
        assertThat(sellerCurrencySpec.getFilterKey()).isEqualTo("sellerCurrency");
        assertThat(buyerCurrencySpec.getFilterKey()).isEqualTo("buyerCurrency");
        assertThat(soldAmountSpec.getFilterKey()).isEqualTo("soldAmount");
        assertThat(purchasedAmountSpec.getFilterKey()).isEqualTo("purchasedAmount");
        assertThat(exchangeRateSpec.getFilterKey()).isEqualTo("exchangeRate");
        assertThat(createdBetweenSpec.getFilterKey()).isEqualTo("created");
        assertThat(updatedBetweenSpec.getFilterKey()).isEqualTo("updated");
    }
}
