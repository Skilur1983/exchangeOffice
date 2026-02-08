package org.example.exchangeOffice.repository;

import org.example.model.User;
import org.example.model.CurrencyBalance;
import org.example.model.RoleName;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(
        scripts = {
                "/db/migration/create_tables.sql",
                "/db/migration/insert_fixtures.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByIdWithBalances_WhenUserHasMultipleBalances_ReturnUserWithAllBalances() {
        Integer userId = 3;

        Optional<User> result = userRepository.findByIdWithBalances(userId);

        assertThat(result).isPresent();

        User user = result.get();
        assertThat(user.getId()).isEqualTo(3);
        assertThat(user.getUsername()).isEqualTo("SarSmi");
        assertThat(user.getRole()).isEqualTo(RoleName.CUSTOMER);

        assertThat(user.getBalances()).isNotNull();
        assertThat(user.getBalances()).hasSize(2);

        assertThat(user.getBalances())
                .extracting(CurrencyBalance::getCurrency)
                .containsExactlyInAnyOrder(
                        org.example.model.Currency.USD,
                        org.example.model.Currency.EUR
                );

        CurrencyBalance usdBalance = user.getBalances().stream()
                .filter(b -> b.getCurrency() == org.example.model.Currency.USD)
                .findFirst()
                .orElseThrow();
        assertThat(usdBalance.getAmount()).isEqualByComparingTo("5000.00");

        CurrencyBalance eurBalance = user.getBalances().stream()
                .filter(b -> b.getCurrency() == org.example.model.Currency.EUR)
                .findFirst()
                .orElseThrow();
        assertThat(eurBalance.getAmount()).isEqualByComparingTo("3000.00");
    }

    @Test
    void findByIdWithBalances_WhenUserHasAllThreeCurrencies_ReturnsAllThree() {
        Integer userId = 4;

        Optional<User> result = userRepository.findByIdWithBalances(userId);

        assertThat(result).isPresent();

        User user = result.get();
        assertThat(user.getUsername()).isEqualTo("TomBro");
        assertThat(user.getBalances()).hasSize(3);

        assertThat(user.getBalances())
                .extracting(CurrencyBalance::getCurrency)
                .containsExactlyInAnyOrder(
                        org.example.model.Currency.USD,
                        org.example.model.Currency.EUR,
                        org.example.model.Currency.UAH
                );
    }

    @Test
    void findByIdWithBalances_WhenUserHasSingleBalance_ReturnsOne() {
        Integer userId = 6;

        Optional<User> result = userRepository.findByIdWithBalances(userId);

        assertThat(result).isPresent();

        User user = result.get();
        assertThat(user.getUsername()).isEqualTo("MarJon");
        assertThat(user.getBalances()).hasSize(1);

        CurrencyBalance balance = user.getBalances().iterator().next();
        assertThat(balance.getCurrency()).isEqualTo(org.example.model.Currency.EUR);
        assertThat(balance.getAmount()).isEqualByComparingTo("10000.00");
    }

    @Test
    void findByIdWithBalances_WhenUserHasNoBalances_ReturnsUserWithEmptyBalances() {
        Integer userId = 2;

        Optional<User> result = userRepository.findByIdWithBalances(userId);

        assertThat(result).isPresent();

        User user = result.get();
        assertThat(user.getUsername()).isEqualTo("admin_second");
        assertThat(user.getRole()).isEqualTo(RoleName.ADMIN);

        assertThat(user.getBalances()).isNotNull();
        assertThat(user.getBalances()).isEmpty();
    }

    @Test
    void findByIdWithBalances_WhenUserDoesNotExist_ReturnsEmpty() {
        Integer nonExistentUserId = 9999;

        Optional<User> result = userRepository.findByIdWithBalances(nonExistentUserId);

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdWithBalances_VerifiesNoLazyLoadingException() {
        Integer userId = 3;

        Optional<User> result = userRepository.findByIdWithBalances(userId);

        assertThat(result).isPresent();
        User user = result.get();

        assertThat(user.getBalances()).isNotEmpty();

        user.getBalances().forEach(balance -> {
            assertThat(balance.getCurrency()).isNotNull();
            assertThat(balance.getAmount()).isNotNull();
            assertThat(balance.getUser()).isEqualTo(user);
        });
    }
}
