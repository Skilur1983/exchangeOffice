package org.example.exchangeOffice.controller.admin;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminCurrencyBalanceControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/admin/balances";

    @Test
    void getAllBalances_WithoutFilters_ReturnsAllBalances() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements").value(19))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void getAllBalances_WithPagination_ReturnsCorrectPage() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(19))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getAllBalances_FilterByUserId_ReturnsUserBalances() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("userId", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void getAllBalances_FilterByCurrency_ReturnsOnlyThatCurrency() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].currency", everyItem(is("USD"))));
    }

    @Test
    void getAllBalances_FilterByAmountBetween_ReturnsBalancesInRange() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("amountBetween", "1000,5000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
    }

    @Test
    void getAllBalances_CombinedFilters_ReturnsFilteredResults() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("userId", "3")
                        .param("currency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].currency").value("USD"));
    }

    @Test
    void getBalanceById_ExistingBalance_ReturnsBalance() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/4")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").isNumber());
    }

    @Test
    void getBalanceById_NonExistentBalance_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Currency Balance with ID: 9999 not found"));
    }

    @Test
    void getBalancesByUserId_UserWithMultipleBalances_ReturnsAll() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/4")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getBalancesByUserId_UserWithSingleBalance_ReturnsOne() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/6")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].currency").value("EUR"));
    }

    @Test
    void getBalancesByUserId_UserWithNoBalances_ReturnsEmptyArray() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/2")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getBalancesByUserId_NonExistentUser_Returns200EmptyArray() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getBalanceByUserAndCurrency_ExistingBalance_ReturnsBalance() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/3/currency/USD")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(5000.0));
    }

    @Test
    void getBalanceByUserAndCurrency_NonExistentBalance_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/3/currency/UAH")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getBalanceByUserAndCurrency_InvalidCurrency_Returns400() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/user/3/currency/INVALID")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createBalance_ValidData_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newBalance = Map.of(
                "userId", 2,
                "currency", "USD",
                "amount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBalance)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(1000.0));
    }

    @Test
    void createBalance_WithZeroAmount_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newBalance = Map.of(
                "userId", 2,
                "currency", "EUR",
                "amount", 0
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBalance)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(0.0));
    }

    @Test
    void createBalance_MissingUserId_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = "{\"currency\": \"USD\", \"amount\": 1000}";

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.userId").exists());
    }

    @Test
    void createBalance_MissingCurrency_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = "{\"userId\": 2, \"amount\": 1000}";

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currency").exists());
    }

    @Test
    void createBalance_NegativeAmount_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newBalance = Map.of(
                "userId", 2,
                "currency", "USD",
                "amount", -100.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBalance)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createBalance_DuplicateUserCurrency_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> duplicateBalance = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicateBalance)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Balance already exists for user ID 3 and currency USD"));
    }

    @Test
    void createBalance_NonExistentUser_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newBalance = Map.of(
                "userId", 9999,
                "currency", "USD",
                "amount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBalance)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deposit_ToExistingBalance_IncreasesAmount() throws Exception {
        String token = getAdminToken();
        Map<String, Object> deposit = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 500.00,
                "description", "Test deposit"
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(5500.0));
    }

    @Test
    void deposit_CreatesNewBalance_WhenNotExists() throws Exception {
        String token = getAdminToken();
        Map<String, Object> deposit = Map.of(
                "userId", 2,
                "currency", "USD",
                "amount", 1000.00,
                "description", "Initial deposit"
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(1000.0));
    }

    @Test
    void deposit_WithoutDescription_Succeeds() throws Exception {
        String token = getAdminToken();
        Map<String, Object> deposit = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 100.00
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isOk());
    }

    @Test
    void deposit_ZeroAmount_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> deposit = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 0
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deposit_NegativeAmount_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> deposit = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", -100.00
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deposit_NonExistentUser_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, Object> deposit = Map.of(
                "userId", 9999,
                "currency", "USD",
                "amount", 100.00
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdraw_WithSufficientFunds_DecreasesAmount() throws Exception {
        String token = getAdminToken();
        Map<String, Object> withdrawal = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 500.00,
                "description", "Test withdrawal"
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").value(4500.0));
    }

    @Test
    void withdraw_ExactBalance_LeavesZero() throws Exception {
        String token = getAdminToken();
        Map<String, Object> withdrawal = Map.of(
                "userId", 3,
                "currency", "EUR",
                "amount", 3000.00,
                "description", "Full withdrawal"
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.0));
    }

    @Test
    void withdraw_InsufficientFunds_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> withdrawal = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 10000.00
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Insufficient balance. Available: 5000.0000, Required: 10000.0"));
    }

    @Test
    void withdraw_FromNonExistentBalance_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, Object> withdrawal = Map.of(
                "userId", 3,
                "currency", "UAH",
                "amount", 100.00
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdraw_NegativeAmount_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> withdrawal = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", -100.00
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_ZeroAmount_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> withdrawal = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 0
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBalance_ExistingBalance_Returns204() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newBalance = Map.of(
                "userId", 2,
                "currency", "UAH",
                "amount", 0
        );

        String response = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBalance)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int balanceId = fromJsonToMap(response).get("id") instanceof Integer
                ? (Integer) fromJsonToMap(response).get("id")
                : Integer.parseInt(fromJsonToMap(response).get("id").toString());

        mockMvc.perform(delete(BASE_URL + "/" + balanceId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBalance_NonExistentBalance_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void workflow_CreateDepositWithdrawDelete_WorksCorrectly() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newBalance = Map.of(
                "userId", 11,
                "currency", "EUR",
                "amount", 1000.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newBalance)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andReturn().getResponse().getContentAsString();

        int balanceId = (Integer) fromJsonToMap(createResponse).get("id");

        Map<String, Object> deposit = Map.of(
                "userId", 11,
                "currency", "EUR",
                "amount", 500.00
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500.0));

        Map<String, Object> withdrawal = Map.of(
                "userId", 11,
                "currency", "EUR",
                "amount", 1500.00
        );

        mockMvc.perform(post(BASE_URL + "/withdraw")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(withdrawal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(0.0));

        mockMvc.perform(delete(BASE_URL + "/" + balanceId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void workflow_MultipleDepositsWork() throws Exception {
        String token = getAdminToken();

        Map<String, Object> deposit1 = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 100.00
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(greaterThan(5000.0)));

        Map<String, Object> deposit2 = Map.of(
                "userId", 3,
                "currency", "USD",
                "amount", 100.00
        );

        mockMvc.perform(post(BASE_URL + "/deposit")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deposit2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(greaterThan(5100.0)));
    }
}
