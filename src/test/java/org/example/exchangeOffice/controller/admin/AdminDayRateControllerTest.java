package org.example.exchangeOffice.controller.admin;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminDayRateControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/admin/rates";

    @Test
    void getAllRates_WithoutFilters_ReturnsAllRates() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void getAllRates_WithPagination_ReturnsCorrectPage() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getAllRates_FilterByBaseCurrency_ReturnsFilteredRates() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("baseCurrency", "USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].baseCurrency", everyItem(is("USD"))));
    }

    @Test
    void getAllRates_FilterByQuoteCurrency_ReturnsFilteredRates() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("quoteCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].quoteCurrency", everyItem(is("EUR"))));
    }

    @Test
    void getAllRates_CombinedFilters_ReturnsFilteredResults() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("baseCurrency", "USD")
                        .param("quoteCurrency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getRateById_ExistingRate_ReturnsRate() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.baseCurrency").exists())
                .andExpect(jsonPath("$.quoteCurrency").exists())
                .andExpect(jsonPath("$.buyRate").isNumber())
                .andExpect(jsonPath("$.sellRate").isNumber());
    }

    @Test
    void getRateById_NonExistentRate_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Day Rate with ID: 9999 not found"));
    }

    @Test
    void createRate_ValidData_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newRate = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "UAH",
                "rateDate", "2026-12-25",
                "buyRate", 41.20,
                "sellRate", 41.70
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newRate)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.baseCurrency").value("USD"))
                .andExpect(jsonPath("$.quoteCurrency").value("UAH"))
                .andExpect(jsonPath("$.buyRate").value(41.20))
                .andExpect(jsonPath("$.sellRate").value(41.70));
    }

    @Test
    void createRate_MissingBaseCurrency_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "quoteCurrency": "EUR",
                    "rateDate": "2026-12-25",
                    "buyRate": 0.94,
                    "sellRate": 0.97
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.baseCurrency").exists());
    }

    @Test
    void createRate_MissingQuoteCurrency_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "baseCurrency": "USD",
                    "rateDate": "2026-12-25",
                    "buyRate": 0.94,
                    "sellRate": 0.97
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.quoteCurrency").exists());
    }

    @Test
    void createRate_MissingRateDate_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "baseCurrency": "USD",
                    "quoteCurrency": "EUR",
                    "buyRate": 0.94,
                    "sellRate": 0.97
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rateDate").exists());
    }

    @Test
    void createRate_MissingBuyRate_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "baseCurrency": "USD",
                    "quoteCurrency": "EUR",
                    "rateDate": "2026-12-25",
                    "sellRate": 0.97
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.buyRate").exists());
    }

    @Test
    void createRate_MissingSellRate_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = """
                {
                    "baseCurrency": "USD",
                    "quoteCurrency": "EUR",
                    "rateDate": "2026-12-25",
                    "buyRate": 0.94
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.sellRate").exists());
    }

    @Test
    void createRate_SellRateLowerThanBuyRate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newRate = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "EUR",
                "rateDate", "2026-12-25",
                "buyRate", 0.97,
                "sellRate", 0.94
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newRate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createRate_SameCurrency_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newRate = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "USD",
                "rateDate", "2026-12-25",
                "buyRate", 0.94,
                "sellRate", 0.97
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newRate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createRate_DuplicateCurrencyPairAndDate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> duplicateRate = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "EUR",
                "rateDate", "2024-12-23",
                "buyRate", 0.94,
                "sellRate", 0.97
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicateRate)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRate_ValidBuyRate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("buyRate", 0.95);

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRate_ValidSellRate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("sellRate", 1.05);

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRate_BothRates_Returns200() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of(
                "buyRate", 0.96,
                "sellRate", 0.99
        );

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyRate").value(0.96))
                .andExpect(jsonPath("$.sellRate").value(0.99));
    }

    @Test
    void updateRate_NonExistentRate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("buyRate", 0.95);

        mockMvc.perform(put(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRate_SellRateLowerThanBuyRate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of(
                "buyRate", 1.00,
                "sellRate", 0.90
        );

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateRate_NegativeRate_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("buyRate", -0.95);

        mockMvc.perform(put(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteRate_ExistingRate_Returns204() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newRate = Map.of(
                "baseCurrency", "EUR",
                "quoteCurrency", "UAH",
                "rateDate", "2026-12-26",
                "buyRate", 43.80,
                "sellRate", 44.30
        );

        String response = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newRate)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        int rateId = (Integer) fromJsonToMap(response).get("id");

        mockMvc.perform(delete(BASE_URL + "/" + rateId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/" + rateId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRate_NonExistentRate_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void workflow_CreateUpdateDelete_WorksCorrectly() throws Exception {
        String token = getAdminToken();

        Map<String, Object> newRate = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "EUR",
                "rateDate", "2026-12-27",
                "buyRate", 0.94,
                "sellRate", 0.97
        );

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newRate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.buyRate").value(0.94))
                .andReturn().getResponse().getContentAsString();

        int rateId = (Integer) fromJsonToMap(createResponse).get("id");

        Map<String, Object> update = Map.of(
                "buyRate", 0.95,
                "sellRate", 0.98
        );

        mockMvc.perform(put(BASE_URL + "/" + rateId)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.buyRate").value(0.95))
                .andExpect(jsonPath("$.sellRate").value(0.98));

        mockMvc.perform(delete(BASE_URL + "/" + rateId)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void createRate_MultipleCurrencyPairs_AllSucceed() throws Exception {
        String token = getAdminToken();

        Map<String, Object> usdEur = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "EUR",
                "rateDate", "2026-12-28",
                "buyRate", 0.94,
                "sellRate", 0.97
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(usdEur)))
                .andExpect(status().isCreated());

        Map<String, Object> eurUah = Map.of(
                "baseCurrency", "EUR",
                "quoteCurrency", "UAH",
                "rateDate", "2026-12-28",
                "buyRate", 43.80,
                "sellRate", 44.30
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(eurUah)))
                .andExpect(status().isCreated());

        Map<String, Object> usdUah = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "UAH",
                "rateDate", "2026-12-28",
                "buyRate", 41.20,
                "sellRate", 41.70
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(usdUah)))
                .andExpect(status().isCreated());
    }

    @Test
    void createRate_SamePairDifferentDates_BothSucceed() throws Exception {
        String token = getAdminToken();

        Map<String, Object> rate1 = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "EUR",
                "rateDate", "2026-12-29",
                "buyRate", 0.94,
                "sellRate", 0.97
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(rate1)))
                .andExpect(status().isCreated());

        Map<String, Object> rate2 = Map.of(
                "baseCurrency", "USD",
                "quoteCurrency", "EUR",
                "rateDate", "2026-12-30",
                "buyRate", 0.95,
                "sellRate", 0.98
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(rate2)))
                .andExpect(status().isCreated());
    }
}
