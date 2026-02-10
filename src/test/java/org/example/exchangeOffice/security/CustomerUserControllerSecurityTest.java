package org.example.exchangeOffice.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerUserControllerSecurityTest extends SecurityTestBase {

    private static final String PROFILE_URL = "/customer/profile";
    private static final String PASSWORD_URL = "/customer/profile/password";
    private static final String VALID_NEW_PASSWORD = "NewPassword123@";

    @Test
    void getMyProfile_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(PROFILE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_WithInvalidToken_Returns401() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(invalidToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMyProfile_WithMalformedAuthHeader_Returns403() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_WithAdminToken_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_WithCustomerToken_Returns200WithOwnProfile() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("SarSmi"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void getMyProfile_WithDifferentCustomerToken_Returns200WithCorrectProfile() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.username").value("TomBro"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void getMyProfile_DoesNotExposePassword() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void changePassword_WithoutAuth_Returns403() throws Exception {
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_WithAdminToken_Returns403() throws Exception {
        String token = getAdminToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "admin123password",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_WithValidData_Returns204() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_WithIncorrectCurrentPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "wrong_password",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void changePassword_WithMismatchedPasswords_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", "DifferentPassword123@"
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_WithMissingCurrentPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        String requestJson = """
                {
                    "newPassword": "%s",
                    "confirmPassword": "%s"
                }
                """.formatted(VALID_NEW_PASSWORD, VALID_NEW_PASSWORD);

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.currentPassword").exists());
    }

    @Test
    void changePassword_WithMissingNewPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        String requestJson = """
                {
                    "currentPassword": "customer123",
                    "confirmPassword": "%s"
                }
                """.formatted(VALID_NEW_PASSWORD);

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void changePassword_WithWeakPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", "weak",
                "confirmPassword", "weak"
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void changePassword_WithEmptyCurrentPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.currentPassword").exists());
    }

    @Test
    void changePassword_AfterChange_OldPasswordNoLongerWorks() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isNoContent());

        Map<String, String> loginRequest = Map.of(
                "username", "SarSmi",
                "password", "customer123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_AfterChange_NewPasswordWorks() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isNoContent());

        Map<String, String> loginRequest = Map.of(
                "username", "SarSmi",
                "password", VALID_NEW_PASSWORD
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void getMyProfile_DifferentCustomers_GetDifferentProfiles() throws Exception {
        String sarSmiToken = getCustomerToken();
        String tomBroToken = getSecondCustomerToken();

        String sarSmiProfile = mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(sarSmiToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String tomBroProfile = mockMvc.perform(get(PROFILE_URL)
                        .header("Authorization", bearerToken(tomBroToken)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assert !sarSmiProfile.equals(tomBroProfile);
    }

    @Test
    void changePassword_OneCustomer_DoesNotAffectOtherCustomers() throws Exception {
        String sarSmiToken = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(PASSWORD_URL)
                        .header("Authorization", bearerToken(sarSmiToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChange)))
                .andExpect(status().isNoContent());

        Map<String, String> tomBroLogin = Map.of(
                "username", "TomBro",
                "password", "customer123"
        );

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tomBroLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void multipleProfileRequests_WithSameToken_AllSucceed() throws Exception {
        String token = getCustomerToken();

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get(PROFILE_URL)
                            .header("Authorization", bearerToken(token)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("SarSmi"));
        }
    }

    @Test
    void changePassword_WithNullRequestBody_Returns400() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(patch(PASSWORD_URL)
                .header("Authorization", bearerToken(token))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
