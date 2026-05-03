package com.streamflix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamflix.dto.LoginRequest;
import com.streamflix.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import jakarta.annotation.PostConstruct;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private ObjectMapper          mapper;

    private MockMvc mvc;

    @PostConstruct
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    private String uniq() { return UUID.randomUUID().toString().substring(0, 8); }

    @Test
    void register_then_login_succeeds() throws Exception {
        String u = "alice_" + uniq();
        RegisterRequest reg = new RegisterRequest(u, u + "@x.com",
                "password123", "Alice", "VN");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));

        LoginRequest login = new LoginRequest(u, "password123");
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists());
    }

    @Test
    void login_with_wrong_password_returns_401() throws Exception {
        String u = "bob_" + uniq();
        RegisterRequest reg = new RegisterRequest(u, u + "@x.com",
                "password123", "Bob", "VN");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        LoginRequest bad = new LoginRequest(u, "wrong-password");
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_duplicate_username_returns_400() throws Exception {
        String u = "dupe_" + uniq();
        RegisterRequest reg = new RegisterRequest(u, "a_" + u + "@x.com",
                "password123", "Carol", "VN");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(reg)))
                .andExpect(status().isOk());

        RegisterRequest dupe = new RegisterRequest(u, "b_" + u + "@x.com",
                "password123", "Carol2", "VN");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dupe)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void register_invalid_email_returns_400() throws Exception {
        RegisterRequest bad = new RegisterRequest("xyz_" + uniq(), "not-an-email",
                "password123", "X", "VN");
        mvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void public_health_endpoint_does_not_require_auth() throws Exception {
        mvc.perform(get("/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));
    }

    @Test
    void me_without_token_returns_401() throws Exception {
        mvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
