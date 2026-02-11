package org.example.exchangeOffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {
        "/db/migration/create_tables.sql",
        "/db/migration/insert_fixtures.sql"
})
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String getAdminToken() throws Exception {
        return getTokenForUser("admin_first", "admin123password");
    }

    protected String getSecondAdminToken() throws Exception {
        return getTokenForUser("admin_second", "admin123password");
    }

    protected String getThirdAdminToken() throws Exception {
        return getTokenForUser("admin_third", "admin123password");
    }

    protected String getCustomerToken() throws Exception {
        return getTokenForUser("SarSmi", "customer123");
    }

    protected String getSecondCustomerToken() throws Exception {
        return getTokenForUser("TomBro", "customer123");
    }

    protected String getThirdCustomerToken() throws Exception {
        return getTokenForUser("JohDoe", "customer123");
    }

    protected String getTokenForUser(String username, String password) throws Exception {
        Map<String, String> loginRequest = Map.of(
                "username", username,
                "password", password
        );

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

        return (String) response.get("token");
    }

    protected String bearerToken(String token) {
        return "Bearer " + token;
    }

    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> fromJsonToMap(String json) throws Exception {
        return objectMapper.readValue(json, Map.class);
    }
}
