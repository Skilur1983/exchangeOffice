package org.example.exchangeOffice.controller.admin;

import org.example.exchangeOffice.IntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminUserControllerTest extends IntegrationTestBase {

    private static final String BASE_URL = "/admin/users";

    @Test
    void getAllUsers_WithoutFilters_ReturnsAllUsers() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").isNumber());
    }

    @Test
    void getAllUsers_WithPagination_ReturnsCorrectPage() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void getAllUsers_SecondPage_ReturnsRemainingUsers() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.totalElements").value(11));
    }

    @Test
    void getAllUsers_FilterByRole_ReturnsOnlyAdmins() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].role", everyItem(is("ADMIN"))));
    }

    @Test
    void getAllUsers_FilterByRole_ReturnsOnlyCustomers() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(8)))
                .andExpect(jsonPath("$.content[*].role", everyItem(is("CUSTOMER"))));
    }

    @Test
    void getAllUsers_FilterByUsername_ReturnsMatchingUsers() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[*].username", everyItem(containsString("admin"))));
    }

    @Test
    void getAllUsers_FilterByUsernamePartial_ReturnsMatches() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("username", "Tom"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("TomBro"));
    }

    @Test
    void getAllUsers_CombinedFilters_ReturnsFilteredResults() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", bearerToken(token))
                        .param("role", "CUSTOMER")
                        .param("username", "o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.content[*].role", everyItem(is("CUSTOMER"))));
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/3")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("SarSmi"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUserById_Admin_ReturnsAdminUser() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/1")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("admin_first"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void getUserById_NonExistentUser_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User with ID: 9999 not found"));
    }

    @Test
    void getUserById_InvalidId_Returns400() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(get(BASE_URL + "/invalid")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createUser_ValidCustomer_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "new_customer_test",
                "password", "ValidPass123@",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newUser)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("new_customer_test"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createUser_ValidAdmin_Returns201() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "new_admin_test",
                "password", "AdminPass123@",
                "role", "ADMIN"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new_admin_test"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void createUser_MissingUsername_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = "{\"password\": \"ValidPass123@\", \"role\": \"CUSTOMER\"}";

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.username").value("Username is required"));
    }

    @Test
    void createUser_MissingPassword_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = "{\"username\": \"testuser\", \"role\": \"CUSTOMER\"}";

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.password").value("Password is required"));
    }

    @Test
    void createUser_MissingRole_Returns400() throws Exception {
        String token = getAdminToken();
        String requestJson = "{\"username\": \"testuser\", \"password\": \"ValidPass123@\"}";

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.role").exists())
                .andExpect(jsonPath("$.errors.role").value("Role is required"));
    }

    @Test
    void createUser_UsernameTooShort_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "ab",
                "password", "ValidPass123@",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username must be between 3 and 100 characters"));
    }

    @Test
    void createUser_UsernameWithInvalidCharacters_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "user@name!",
                "password", "ValidPass123@",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username can only contain letters, numbers, underscores, and hyphens"));
    }

    @Test
    void createUser_PasswordTooShort_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "testuser",
                "password", "Pass1@",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password must be between 8 and 100 characters"));
    }

    @Test
    void createUser_PasswordMissingRequiredCharacters_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> newUser = Map.of(
                "username", "testuser",
                "password", "weakpassword",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(newUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("Password must contain at least one digit, one lowercase letter, one uppercase letter, and one special character"));
    }

    @Test
    void createUser_DuplicateUsername_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> duplicateUser = Map.of(
                "username", "SarSmi",
                "password", "ValidPass123@",
                "role", "CUSTOMER"
        );

        mockMvc.perform(post(BASE_URL + "/create")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateUser_ValidUsernameChange_Returns200() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("username", "SarSmi_updated");

        mockMvc.perform(put(BASE_URL + "/update/3")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.username").value("SarSmi_updated"))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void updateUser_ValidRoleChange_Returns200() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("role", "ADMIN");

        mockMvc.perform(put(BASE_URL + "/update/3")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updateUser_NonExistentUser_Returns404() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("username", "newname");

        mockMvc.perform(put(BASE_URL + "/update/9999")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateUser_InvalidUsername_Returns400() throws Exception {
        String token = getAdminToken();
        Map<String, Object> update = Map.of("username", "ab");

        mockMvc.perform(put(BASE_URL + "/update/3")
                        .header("Authorization", bearerToken(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(update)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").value("Username must be between 3 and 100 characters"));
    }

    @Test
    void deleteUser_ExistingUser_Returns204() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/10")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/10")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_NonExistentUser_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/9999")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deleteUser_ThenGetUser_Returns404() throws Exception {
        String token = getAdminToken();

        mockMvc.perform(delete(BASE_URL + "/11")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(BASE_URL + "/11")
                        .header("Authorization", bearerToken(token)))
                .andExpect(status().isNotFound());
    }
}
