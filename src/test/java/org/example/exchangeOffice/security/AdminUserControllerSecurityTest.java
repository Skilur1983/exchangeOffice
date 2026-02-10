package org.example.exchangeOffice.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminUserControllerSecurityTest extends SecurityTestBase {

    private static final String BASE_URL = "/admin/users";

    @Test
    void getUsers_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_WithInvalidToken_Returns401() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(invalidToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsers_WithMalformedAuthHeader_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_WithCustomerToken_Returns403() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_WithAdminToken_Returns200() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    void getUsers_WithDifferentAdminToken_Returns200() throws Exception {
        String token = getSecondAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_WithCustomerToken_Returns403() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_WithAdminToken_Returns200() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/3")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("SarSmi"));
    }

    @Test
    void getUserById_WithAdminToken_NonExistentUser_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createUser_WithoutAuth_Returns403() throws Exception {
        Map<String, Object> newUser = Map.of(
                "username", "newuser",
                "password", "password123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_WithCustomerToken_Returns403() throws Exception {
        String token = getCustomerToken();
        Map<String, Object> newUser = Map.of(
                "username", "newuser",
                "password", "password123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_WithAdminToken_ValidData_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "newcustomer",
                "password", "pasSw*ord123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.username").value("newcustomer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void createUser_WithAdminToken_MissingUsername_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = "{\"password\": \"password123\", \"role\": \"CUSTOMER\"}";

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void createUser_WithAdminToken_DuplicateUsername_Returns405() throws Exception {
        String token = getAdminToken();
        Map<String, Object> duplicateUser = Map.of(
                "username", "SarSmi",
                "password", "password123",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void updateUser_WithoutAuth_Returns403() throws Exception {
        Map<String, Object> update = Map.of("role", "ADMIN");

        mockMvc.perform(put(BASE_URL + "/update/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_WithCustomerToken_Returns403() throws Exception {
        String token = getCustomerToken();
        Map<String, Object> update = Map.of("role", "ADMIN");

        mockMvc.perform(patch(BASE_URL + "/3")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_WithAdminToken_Returns200() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("role", "ADMIN");

        mockMvc.perform(put(BASE_URL + "/update/3")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deleteUser_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/10"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_WithCustomerToken_Returns403() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(delete(BASE_URL + "/10")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_WithAdminToken_Returns204() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/10")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getUsers_WithAdminToken_WithFilters_Returns200() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getUsers_WithAdminToken_WithPagination_Returns200() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.pageNumber").value(0));
    }

    @Test
    void multipleRequests_WithSameToken_AllSucceed() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL).header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/1").header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL).param("role", "ADMIN").header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_WithEmptyAuthHeader_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", ""))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_WithOnlyBearerKeyword_Returns401() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());
    }
}
