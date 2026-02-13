package org.example.exchangeOffice.controller.customer;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerUserControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/customer/profile";
    private static final String VALID_NEW_PASSWORD = "NewPassword123@";

    @Test
    void getMyProfile_AsCustomer_ReturnsProfile() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("SarSmi"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void getMyProfile_SecondCustomer_ReturnsProfile() throws Exception {
        String token = getSecondCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.username").value("TomBro"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void getMyProfile_ThirdCustomer_ReturnsProfile() throws Exception {
        String token = getThirdCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("JohDoe"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void getMyProfile_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMyProfile_WithoutAuth_Returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_ValidData_Returns204() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changePassword_IncorrectCurrentPassword_Returns400() throws Exception {
        String token = getSecondCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "WrongPassword123@",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_PasswordMismatch_Returns400() throws Exception {
        String token = getThirdCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", "DifferentPassword123@"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void changePassword_WeakPasswordTooShort_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "Short1@",
                "confirmPassword", "Short1@"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_MissingUppercase_Returns400() throws Exception {
        String token = getSecondCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "newpassword123@",
                "confirmPassword", "newpassword123@"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_MissingLowercase_Returns400() throws Exception {
        String token = getThirdCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "NEWPASSWORD123@",
                "confirmPassword", "NEWPASSWORD123@"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_MissingDigit_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "NewPassword@",
                "confirmPassword", "NewPassword@"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_MissingSpecialChar_Returns400() throws Exception {
        String token = getSecondCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "NewPassword123",
                "confirmPassword", "NewPassword123"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_InvalidSpecialChar_Returns400() throws Exception {
        String token = getThirdCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "NewPassword123!",
                "confirmPassword", "NewPassword123!"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void changePassword_SameAsCurrentPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", "password",
                "confirmPassword", "password"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."));
    }

    @Test
    void changePassword_MissingCurrentPassword_Returns400() throws Exception {
        String token = getSecondCustomerToken();
        String requestJson = """
                {
                    "newPassword": "NewPassword123@",
                    "confirmPassword": "NewPassword123@"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.currentPassword").exists());
    }

    @Test
    void changePassword_MissingNewPassword_Returns400() throws Exception {
        String token = getThirdCustomerToken();
        String requestJson = """
                {
                    "currentPassword": "password",
                    "confirmPassword": "NewPassword123@"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void changePassword_MissingConfirmPassword_Returns400() throws Exception {
        String token = getCustomerToken();
        String requestJson = """
                {
                    "currentPassword": "password",
                    "newPassword": "NewPassword123@"
                }
                """;

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.confirmPassword").exists());
    }

    @Test
    void changePassword_AsAdmin_Returns403() throws Exception {
        String token = getAdminToken();
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_WithoutAuth_Returns403() throws Exception {
        Map<String, String> passwordChange = Map.of(
                "currentPassword", "password",
                "newPassword", VALID_NEW_PASSWORD,
                "confirmPassword", VALID_NEW_PASSWORD
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isForbidden());
    }

    @Test
    void workflow_GetProfileThenChangePassword_WorksCorrectly() throws Exception {
        String token = getCustomerToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("SarSmi"));

        Map<String, String> passwordChange = Map.of(
                "currentPassword", "customer123",
                "newPassword", "MyNewPassword123@",
                "confirmPassword", "MyNewPassword123@"
        );

        mockMvc.perform(patch(BASE_URL + "/password")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(passwordChange)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk());
    }
}
