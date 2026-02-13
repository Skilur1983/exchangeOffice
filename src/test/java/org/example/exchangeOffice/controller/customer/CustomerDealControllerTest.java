package org.example.exchangeOffice.controller.customer;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerDealControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/customer/deals";

    @Test
    void getMyDeals_AsCustomer_ReturnsOnlyMyDeals() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageNumber").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void getMyDeals_WithStatusFilter_ReturnsFilteredDeals() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMyDeals_WithDealTypeFilter_ReturnsFilteredDeals() throws Exception {
        String token = getThirdCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("dealType", "SELL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getMyDeals_WithPagination_ReturnsCorrectPage() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    @Test
    void getMyDeals_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyDeals_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyDealById_OwnDeal_ReturnsDeal() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dealType").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void getMyDealById_OtherCustomersDeal_Returns403() throws Exception {
        String token = getThirdCustomerToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You can only access your own deals"));
    }

    @Test
    void getMyDealById_NonExistentDeal_Returns404() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getMyDealById_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyDealById_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDeal_CustomerAsSeller_Returns201() throws Exception {
        String token = getCustomerToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "BUY",
                "sellerId", 3,
                "buyerId", 1,
                "sellerCurrency", "EUR",
                "buyerCurrency", "USD",
                "purchasedAmount", 100.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dealType").value("BUY"));
    }

    @Test
    void createDeal_CustomerAsBuyer_Returns201() throws Exception {
        String token = getSecondCustomerToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 4,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 500.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealType").value("SELL"));
    }

    @Test
    void createDeal_CustomerNotParticipant_Returns403() throws Exception {
        String token = getCustomerToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 4,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 500.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You must be a participant in the deal (seller or buyer)"));
    }

    @Test
    void createDeal_MissingDealType_Returns400() throws Exception {
        String token = getSecondCustomerToken();
        String requestJson = """
                {
                    "sellerId": 4,
                    "buyerId": 1,
                    "sellerCurrency": "EUR",
                    "buyerCurrency": "USD",
                    "soldAmount": 100.00
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.dealType").exists());
    }

    @Test
    void createDeal_SameCurrencies_Returns400() throws Exception {
        String token = getThirdCustomerToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "BUY",
                "sellerId", 5,
                "buyerId", 1,
                "sellerCurrency", "USD",
                "buyerCurrency", "USD",
                "soldAmount", 100.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createDeal_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 100.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createDeal_WithoutAuth_Returns403() throws Exception {
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 100.00
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isForbidden());
    }

    @Test
    void resumeMyDeal_OwnPausedDeal_Returns200() throws Exception {
        String token = getCustomerToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 100.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int dealId = (Integer) fromJsonToMap(createResponse).get("id");
        String status = (String) fromJsonToMap(createResponse).get("status");

        if ("PAUSED".equals(status)) {
            mockMvc.perform(patch(BASE_URL + "/" + dealId + "/resume")
                            .header("Authorization", bearerToken(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }
    }

    @Test
    void resumeMyDeal_OtherCustomersDeal_Returns403() throws Exception {
        String token = getThirdCustomerToken();

        mockMvc.perform(patch(BASE_URL + "/1/resume")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access your own deals"));
    }

    @Test
    void resumeMyDeal_NonExistentDeal_Returns404() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(patch(BASE_URL + "/9999/resume")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void resumeMyDeal_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(patch(BASE_URL + "/1/resume")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void resumeMyDeal_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/1/resume"))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelMyDeal_OwnDeal_Returns200() throws Exception {
        String token = getSecondCustomerToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 4,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 50000.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int dealId = (Integer) fromJsonToMap(createResponse).get("id");

        Map<String, String> cancelRequest = Map.of("cancellationReason", "Changed my mind");

        mockMvc.perform(patch(BASE_URL + "/" + dealId + "/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelMyDeal_OtherCustomersDeal_Returns403() throws Exception {
        String token = getCustomerToken();
        Map<String, String> cancelRequest = Map.of("cancellationReason", "Test");

        mockMvc.perform(patch(BASE_URL + "/2/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access your own deals"));
    }

    @Test
    void cancelMyDeal_MissingReason_Returns400() throws Exception {
        String token = getSecondCustomerToken();
        String requestJson = "{}";

        mockMvc.perform(patch(BASE_URL + "/1/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void cancelMyDeal_NonExistentDeal_Returns404() throws Exception {
        String token = getThirdCustomerToken();
        Map<String, String> cancelRequest = Map.of("cancellationReason", "Test");

        mockMvc.perform(patch(BASE_URL + "/9999/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelMyDeal_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();
        Map<String, String> cancelRequest = Map.of("cancellationReason", "Test");

        mockMvc.perform(patch(BASE_URL + "/1/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelMyDeal_WithoutAuth_Returns403() throws Exception {
        Map<String, String> cancelRequest = Map.of("cancellationReason", "Test");

        mockMvc.perform(patch(BASE_URL + "/1/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void workflow_CreateAndCancelDeal_WorksCorrectly() throws Exception {
        String token = getCustomerToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 100000.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int dealId = (Integer) fromJsonToMap(createResponse).get("id");

        mockMvc.perform(get(BASE_URL + "/" + dealId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dealId));

        Map<String, String> cancelRequest = Map.of("cancellationReason", "Workflow test");

        mockMvc.perform(patch(BASE_URL + "/" + dealId + "/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void workflow_VerifyOwnershipProtection_PreventsCrossAccess() throws Exception {
        String customer1Token = getCustomerToken();
        String customer2Token = getSecondCustomerToken();

        Map<String, Object> deal1 = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 50.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(customer1Token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deal1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int dealId = (Integer) fromJsonToMap(createResponse).get("id");

        mockMvc.perform(get(BASE_URL + "/" + dealId)
                        .header("Authorization", bearerToken(customer2Token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access your own deals"));
    }
}
