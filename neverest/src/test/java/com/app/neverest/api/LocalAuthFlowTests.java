package com.app.neverest.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LocalAuthFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginWithSeededUserReturnsToken() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "serbancorodescu14@gmail.com",
                                          "password": "123456"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginWithInvalidPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "serbancorodescu14@gmail.com",
                                          "password": "wrong-password"
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void normalUserCannotCreateEventWithLocalJwt() throws Exception {
        String token = loginAndReadToken("serbancorodescu14@gmail.com", "123456");

        mockMvc.perform(
                        post("/api/v1/events")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "User forbidden event",
                                          "activityType": "RUNNING",
                                          "location": "Test location",
                                          "startsAt": "2026-06-01T18:30:00",
                                          "pointsReward": 50
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateEventWithLocalJwt() throws Exception {
        String token = loginAndReadToken("neverest@gmail.com", "123456");

        mockMvc.perform(
                        post("/api/v1/events")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Admin event",
                                          "activityType": "RUNNING",
                                          "location": "Admin location",
                                          "startsAt": "2026-06-01T19:00:00",
                                          "pointsReward": 60
                                        }
                                        """)
                )
                .andExpect(status().isCreated());
    }

    private String loginAndReadToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "%s",
                                          "password": "%s"
                                        }
                                        """.formatted(email, password))
                )
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("accessToken").asText();
    }
}
