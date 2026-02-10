package org.example.exchangeOffice.repository.specification;

import org.example.model.RoleName;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.repository.specification.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "org.example.repository.specification.user")
@Sql(
        scripts = {
                "/db/migration/create_tables.sql",
                "/db/migration/insert_fixtures.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public class UserSpecificationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleSpecification roleSpec;

    @Autowired
    private UsernameSpecification usernameSpec;

    @Autowired
    private CreatedBetweenUserSpecification createdBetweenSpec;

    @Autowired
    private UpdatedBetweenUserSpecification updatedBetweenSpec;

    @Test
    void roleSpec_WhenFilteringAdmins_ReturnsOnlyAdmins() {
        String[] params = {"ADMIN"};

        Specification<User> spec = roleSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(u -> u.getRole() == RoleName.ADMIN);

        assertThat(results).hasSize(3);
        assertThat(results)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("admin_first", "admin_second", "admin_third");
    }

    @Test
    void roleSpec_WhenFilteringCustomers_ReturnsOnlyCustomers() {
        String[] params = {"CUSTOMER"};

        Specification<User> spec = roleSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(u -> u.getRole() == RoleName.CUSTOMER);

        assertThat(results).hasSize(8);
        assertThat(results)
                .extracting(User::getUsername)
                .contains("SarSmi", "TomBro", "JohDoe", "MarJon", "AnnWil", "PetBak", "LisChe", "DavMil");
    }

    @Test
    void roleSpec_WhenLowercaseRole_ConvertsToUppercase() {
        String[] params = {"customer"};

        Specification<User> spec = roleSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(8);
        assertThat(results).allMatch(u -> u.getRole() == RoleName.CUSTOMER);
    }

    @Test
    void roleSpec_WhenInvalidRole_ReturnsEmpty() {
        String[] params = {"INVALID_ROLE"};

        Specification<User> spec = roleSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void roleSpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<User> spec = roleSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void roleSpec_WhenEmptyParams_ReturnsAll() {
        String[] params = {};

        Specification<User> spec = roleSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void usernameSpec_WhenSearchingExactUsername_ReturnsUser() {
        String[] params = {"SarSmi"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("SarSmi");
    }

    @Test
    void usernameSpec_WhenSearchingPartialUsername_ReturnsMatches() {
        String[] params = {"admin"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(3);
        assertThat(results)
                .extracting(User::getUsername)
                .allMatch(username -> username.toLowerCase().contains("admin"));
    }

    @Test
    void usernameSpec_WhenSearchingPrefix_ReturnsMatches() {
        String[] params = {"Tom"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("TomBro");
    }

    @Test
    void usernameSpec_WhenSearchingSuffix_ReturnsMatches() {
        String[] params = {"Smi"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("SarSmi");
    }

    @Test
    void usernameSpec_WhenCaseInsensitiveSearch_FindsMatch() {
        String[] params = {"SARSMI"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("SarSmi");
    }

    @Test
    void usernameSpec_WhenSearchingCommonPattern_ReturnsMultiple() {
        String[] params = {"o"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSizeGreaterThanOrEqualTo(3);
        assertThat(results)
                .extracting(User::getUsername)
                .contains("JohDoe", "TomBro", "MarJon");
    }

    @Test
    void usernameSpec_WhenNoMatch_ReturnsEmpty() {
        String[] params = {"NonExistentUser"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void usernameSpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void usernameSpec_WhenEmptyString_ReturnsAll() {
        String[] params = {""};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void usernameSpec_WhenBlankString_ReturnsAll() {
        String[] params = {"   "};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void createdBetweenSpec_WhenFilteringByToday_ReturnsAllUsers() {
        String today = LocalDate.now().toString();
        String[] params = {today};

        Specification<User> spec = createdBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void createdBetweenSpec_WhenFilteringFutureDates_ReturnsEmpty() {
        String[] params = {"2025-12-01", "2025-12-31"};

        Specification<User> spec = createdBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void createdBetweenSpec_WhenFilteringPastDates_ReturnsEmpty() {
        String[] params = {"2020-01-01", "2020-12-31"};

        Specification<User> spec = createdBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void createdBetweenSpec_WhenOnlyStartDate_FiltersGreaterThanOrEqual() {
        String today = LocalDate.now().toString();
        String[] params = {today};

        Specification<User> spec = createdBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void createdBetweenSpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<User> spec = createdBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void updatedBetweenSpec_WhenFilteringByToday_ReturnsAllUsers() {
        String today = LocalDate.now().toString();
        String[] params = {today};

        Specification<User> spec = updatedBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void updatedBetweenSpec_WhenFilteringFutureDates_ReturnsEmpty() {
        String[] params = {"2025-12-01", "2025-12-31"};

        Specification<User> spec = updatedBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).isEmpty();
    }

    @Test
    void updatedBetweenSpec_WhenNullParams_ReturnsAll() {
        String[] params = null;

        Specification<User> spec = updatedBetweenSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(11);
    }

    @Test
    void combinedSpecs_RoleAndUsername_ReturnsIntersection() {
        Specification<User> roleSp = roleSpec.getSpecification(new String[]{"ADMIN"});
        Specification<User> usernameSp = usernameSpec.getSpecification(new String[]{"admin"});

        Specification<User> combined = Specification.where(roleSp).and(usernameSp);

        List<User> results = userRepository.findAll(combined);

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(u -> u.getRole() == RoleName.ADMIN);
        assertThat(results)
                .extracting(User::getUsername)
                .allMatch(username -> username.toLowerCase().contains("admin"));
    }

    @Test
    void combinedSpecs_RoleAndUsernameNoMatch_ReturnsEmpty() {
        Specification<User> roleSp = roleSpec.getSpecification(new String[]{"CUSTOMER"});
        Specification<User> usernameSp = usernameSpec.getSpecification(new String[]{"admin"});

        Specification<User> combined = Specification.where(roleSp).and(usernameSp);

        List<User> results = userRepository.findAll(combined);

        assertThat(results).isEmpty();
    }

    @Test
    void combinedSpecs_UsernamePartial_ReturnsSpecificCustomer() {
        Specification<User> roleSp = roleSpec.getSpecification(new String[]{"CUSTOMER"});
        Specification<User> usernameSp = usernameSpec.getSpecification(new String[]{"Sar"});

        Specification<User> combined = Specification.where(roleSp).and(usernameSp);

        List<User> results = userRepository.findAll(combined);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("SarSmi");
        assertThat(results.get(0).getRole()).isEqualTo(RoleName.CUSTOMER);
    }

    @Test
    void combinedSpecs_ThreeFilters_ReturnsFilteredResults() {
        String todayStart = LocalDate.now().atStartOfDay().toString();
        String todayEnd = LocalDate.now().plusDays(1).atStartOfDay().toString();

        Specification<User> roleSp = roleSpec.getSpecification(new String[]{"CUSTOMER"});
        Specification<User> usernameSp = usernameSpec.getSpecification(new String[]{"o"});
        Specification<User> createdSp = createdBetweenSpec.getSpecification(new String[]{todayStart, todayEnd});

        Specification<User> combined = Specification.where(roleSp)
                .and(usernameSp)
                .and(createdSp);

        List<User> results = userRepository.findAll(combined);

        assertThat(results).hasSizeGreaterThanOrEqualTo(3);
        assertThat(results).allMatch(u -> u.getRole() == RoleName.CUSTOMER);
        assertThat(results)
                .extracting(User::getUsername)
                .allMatch(username -> username.toLowerCase().contains("o"));
    }

    @Test
    void combinedSpecs_RoleOrRole_UsesOrLogic() {
        Specification<User> adminSpec = roleSpec.getSpecification(new String[]{"ADMIN"});
        Specification<User> customerSpec = roleSpec.getSpecification(new String[]{"CUSTOMER"});

        Specification<User> combined = Specification.where(adminSpec).or(customerSpec);

        List<User> results = userRepository.findAll(combined);

        assertThat(results).hasSize(11);
    }

    @Test
    void combinedSpecs_ComplexAndOr_WorksCorrectly() {
        Specification<User> adminSpec = roleSpec.getSpecification(new String[]{"ADMIN"});
        Specification<User> firstSpec = usernameSpec.getSpecification(new String[]{"first"});
        Specification<User> customerSpec = roleSpec.getSpecification(new String[]{"CUSTOMER"});
        Specification<User> sarSpec = usernameSpec.getSpecification(new String[]{"Sar"});

        Specification<User> combined = Specification.where(adminSpec.and(firstSpec))
                .or(customerSpec.and(sarSpec));

        List<User> results = userRepository.findAll(combined);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(User::getUsername)
                .containsExactlyInAnyOrder("admin_first", "SarSmi");
    }

    @Test
    void verifyFilterKeys_AllSpecsHaveCorrectKeys() {
        assertThat(roleSpec.getFilterKey()).isEqualTo("role");
        assertThat(usernameSpec.getFilterKey()).isEqualTo("username");
        assertThat(createdBetweenSpec.getFilterKey()).isEqualTo("created");
        assertThat(updatedBetweenSpec.getFilterKey()).isEqualTo("updated");
    }

    @Test
    void usernameSpec_WhenSearchingWithSpecialCharacters_HandlesGracefully() {
        String[] params = {"_"};

        Specification<User> spec = usernameSpec.getSpecification(params);
        List<User> results = userRepository.findAll(spec);

        assertThat(results).hasSize(3);
        assertThat(results)
                .extracting(User::getUsername)
                .allMatch(username -> username.contains("_"));
    }

    @Test
    void combinedSpecs_EmptyUsername_FiltersByRoleOnly() {
        Specification<User> roleSp = roleSpec.getSpecification(new String[]{"CUSTOMER"});
        Specification<User> usernameSp = usernameSpec.getSpecification(new String[]{""});

        Specification<User> combined = Specification.where(roleSp).and(usernameSp);

        List<User> results = userRepository.findAll(combined);

        assertThat(results).hasSize(8);
        assertThat(results).allMatch(u -> u.getRole() == RoleName.CUSTOMER);
    }
}
