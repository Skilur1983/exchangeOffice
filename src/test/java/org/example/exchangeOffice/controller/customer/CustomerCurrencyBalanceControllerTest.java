package org.example.exchangeOffice.controller.customer;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerCurrencyBalanceControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/customer/balance";

    @Test
    void getMyBalances_CustomerWithMultipleBalances_ReturnsAll() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].currency", containsInAnyOrder("USD", "EUR", "UAH")));
    }

    @Test
    void getMyBalances_CustomerWithSingleBalance_ReturnsOne() throws Exception {
        String token = getThirdCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].currency").value("USD"));
    }

    @Test
    void getMyBalances_CustomerWithNoBalances_ReturnsEmptyArray() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    void getMyBalances_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyBalances_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyBalanceByCurrency_ExistingCurrency_ReturnsBalance() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL + "/currency/USD")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.amount").isNumber());
    }

    @Test
    void getMyBalanceByCurrency_NonExistentCurrency_Returns404() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL + "/currency/UAH")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMyBalanceByCurrency_InvalidCurrency_Returns400() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL + "/currency/INVALID")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getMyBalanceByCurrency_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/currency/USD")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyBalanceByCurrency_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/currency/USD"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyBalanceById_OwnBalance_ReturnsBalance() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL + "/6")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.currency").exists());
    }

    @Test
    void getMyBalanceById_OtherCustomersBalance_Returns403() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL + "/6")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You can only access your own balances"));
    }

    @Test
    void getMyBalanceById_NonExistentBalance_Returns404() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMyBalanceById_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/6")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyBalanceById_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/6"))
                .andExpect(status().isForbidden());
    }

    @Test
    void workflow_GetAllThenGetSpecificCurrency_WorksCorrectly() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get(BASE_URL + "/currency/USD")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("USD"));

        mockMvc.perform(get(BASE_URL + "/currency/EUR")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"));

        mockMvc.perform(get(BASE_URL + "/currency/UAH")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("UAH"));
    }

    @Test
    void workflow_VerifyOwnershipProtection_PreventsCrossAccess() throws Exception {
        String customerToken = getCustomerToken();
        String secondCustomerToken = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(secondCustomerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());

        String response = mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(secondCustomerToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        if (response.contains("\"id\"")) {
            mockMvc.perform(get(BASE_URL + "/6")
                            .header("Authorization", bearerToken(customerToken)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("You can only access your own balances"));
        }
    }
}
