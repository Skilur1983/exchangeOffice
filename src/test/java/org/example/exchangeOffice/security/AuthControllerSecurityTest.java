package org.example.exchangeOffice.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerSecurityTest extends SecurityTestBase {

    @Test
    void login_WithValidAdminCredentials_Returns200WithToken() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin_first",
                "password", "admin123password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_WithValidCustomerCredentials_Returns200WithToken() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "SarSmi",
                "password", "customer123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void login_WithInvalidPassword_Returns401() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin_first",
                "password", "wrong_password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_WithNonExistentUser_Returns401() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "nonexistent_user",
                "password", "any_password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_WithMissingUsername_Returns400() throws Exception {
        String requestJson = "{\"password\": \"admin123password\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.username").value("Username is required"));
    }

    @Test
    void login_WithMissingPassword_Returns400() throws Exception {
        String requestJson = "{\"username\": \"admin_first\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void login_WithEmptyUsername_Returns400() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "",
                "password", "admin123password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.username").value("Username is required"));
    }

    @Test
    void login_WithEmptyPassword_Returns400() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin_first",
                "password", ""
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void login_WithBlankUsername_Returns400() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "   ",
                "password", "admin123password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.username").value("Username is required"));
    }

    @Test
    void login_WithNullRequestBody_Returns400() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body is required but not provided"));
    }

    @Test
    void login_WithMalformedJson_Returns400() throws Exception {
        String malformedJson = "{username: admin_first, password: password}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed JSON or invalid value type. Please ensure all fields have the correct type."));
    }

    @Test
    void login_WithWrongContentType_Returns415() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin_first",
                "password", "admin123password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void login_CaseSensitiveUsername_Returns401WhenWrongCase() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "sarsmi",
                "password", "customer123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_TokenStructure_HasThreeParts() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin_first",
                "password", "admin123password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(matchesPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$")));
    }

    @Test
    void login_DifferentUsers_GetDifferentTokens() throws Exception {
        String token1 = getTokenForUser("admin_first", "admin123password");
        String token2 = getTokenForUser("admin_second", "admin123password");

        assert !token1.equals(token2);
    }

    @Test
    void login_SameUserMultipleCalls_GetsDifferentTokens() throws Exception {
        String token1 = getTokenForUser("admin_first", "admin123password");
        Thread.sleep(1000);
        String token2 = getTokenForUser("admin_first", "admin123password");

        assert !token1.equals(token2);
    }

    @Test
    void login_ResponseHeaders_ContainSecurityHeaders() throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", "admin_first",
                "password", "admin123password"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Type"));
    }

    @Test
    void login_WithGetMethod_Returns405() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("HTTP method 'GET' is not supported for this endpoint. Supported methods: POST"));
    }

    @Test
    void login_WithPutMethod_Returns405() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void login_WithDeleteMethod_Returns405() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/auth/login"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }
}
