package org.example.exchangeOffice.controller.admin;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminDealControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/admin/deals";

    @Test
    void getAllDeals_WithoutFilters_ReturnsAllDeals() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void getAllDeals_WithPagination_ReturnsCorrectPage() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(lessThanOrEqualTo(5))))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    @Test
    void getAllDeals_FilterByStatus_ReturnsFilteredDeals() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].status", everyItem(is("COMPLETED"))));
    }

    @Test
    void getAllDeals_FilterByDealType_ReturnsFilteredDeals() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("dealType", "SELL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].dealType", everyItem(is("SELL"))));
    }

    @Test
    void getDealById_ExistingDeal_ReturnsDeal() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dealType").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.sellerCurrency").exists())
                .andExpect(jsonPath("$.buyerCurrency").exists());
    }

    @Test
    void getDealById_NonExistentDeal_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createDeal_ValidSellDeal_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.dealType").value("SELL"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void createDeal_ValidBuyDeal_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "BUY",
                "sellerId", 4,
                "buyerId", 1,
                "sellerCurrency", "EUR",
                "buyerCurrency", "USD",
                "purchasedAmount", 500.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dealType").value("BUY"));
    }

    @Test
    void createDeal_WithPurchasedAmount_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "BUY",
                "sellerId", 5,
                "buyerId", 1,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "purchasedAmount", 170.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createDeal_MissingDealType_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "sellerId": 1,
                    "buyerId": 3,
                    "sellerCurrency": "USD",
                    "buyerCurrency": "EUR",
                    "soldAmount": 1000.00
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
    void createDeal_MissingSellerId_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "dealType": "SELL",
                    "buyerId": 3,
                    "sellerCurrency": "USD",
                    "buyerCurrency": "EUR",
                    "soldAmount": 1000.00
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sellerId").exists());
    }

    @Test
    void createDeal_MissingBuyerId_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "dealType": "SELL",
                    "sellerId": 1,
                    "sellerCurrency": "USD",
                    "buyerCurrency": "EUR",
                    "soldAmount": 1000.00
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.buyerId").exists());
    }

    @Test
    void createDeal_SameSellerAndBuyer_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 3,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createDeal_SameCurrencies_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "USD",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createDeal_InvalidRoleConfiguration_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "BUY",
                "sellerId", 3,
                "buyerId", 4,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createDeal_NonExistentSeller_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 9999,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createDeal_NonExistentBuyer_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 9999,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createDeal_NoRateAvailable_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "PUR",
                "soldAmount", 1000.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resumeDeal_PausedDeal_Returns200() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 6,
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
            Map<String, String> resumeRequest = Map.of("resumeReason", "Funds deposited");

            mockMvc.perform(patch(BASE_URL + "/" + dealId + "/resume")
                            .header("Authorization", bearerToken(token))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(resumeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }
    }

    @Test
    void resumeDeal_NonExistentDeal_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, String> resumeRequest = Map.of("resumeReason", "Test");

        mockMvc.perform(patch(BASE_URL + "/9999/resume")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(resumeRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelDeal_ExistingDeal_Returns200() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 10000.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int dealId = (Integer) fromJsonToMap(createResponse).get("id");

        Map<String, String> cancelRequest = Map.of("cancellationReason", "Customer request");

        mockMvc.perform(patch(BASE_URL + "/" + dealId + "/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelDeal_NonExistentDeal_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, String> cancelRequest = Map.of("cancellationReason", "Test");

        mockMvc.perform(patch(BASE_URL + "/9999/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteDeal_ExistingDeal_Returns204() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 3,
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

        mockMvc.perform(patch(BASE_URL + "/" + dealId + "/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("cancellationReason", "Test cleanup"))))
                .andExpect(status().isOk());

        mockMvc.perform(delete(BASE_URL + "/" + dealId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteDeal_NonExistentDeal_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void workflow_CreateCancelDelete_WorksCorrectly() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newDeal = Map.of(
                "dealType", "SELL",
                "sellerId", 1,
                "buyerId", 4,
                "sellerCurrency", "USD",
                "buyerCurrency", "EUR",
                "soldAmount", 20000.00
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newDeal)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int dealId = (Integer) fromJsonToMap(createResponse).get("id");

        Map<String, String> cancelRequest = Map.of("cancellationReason", "Workflow test");

        mockMvc.perform(patch(BASE_URL + "/" + dealId + "/cancel")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(cancelRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(delete(BASE_URL + "/" + dealId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + dealId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDeal_MultipleCurrencyPairs_AllSucceed() throws Exception {
        String token = getAdminToken();

        Map<String, Object> deal1 = Map.of(
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
                        .content(toJson(deal1)))
                .andExpect(status().isCreated());

        Map<String, Object> deal2 = Map.of(
                "dealType", "BUY",
                "sellerId", 4,
                "buyerId", 1,
                "sellerCurrency", "EUR",
                "buyerCurrency", "USD",
                "purchasedAmount", 100.00
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(deal2)))
                .andExpect(status().isCreated());
    }
}
