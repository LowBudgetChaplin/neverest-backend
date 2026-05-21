package com.app.neverest.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "neverest.auth.provider=firebase",
        "neverest.firebase.project-id=test-project-id",
        "neverest.integrations.retry.enabled=false"
})
class SecurityAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedCannotAccessProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotCreateEvent() throws Exception {
        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Padel test",
                                          "activityType": "PADEL",
                                          "location": "Test Court",
                                          "startsAt": "2026-06-01T18:00:00",
                                          "pointsReward": 20
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateEvent() throws Exception {
        mockMvc.perform(
                        post("/api/v1/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Padel admin test",
                                          "activityType": "PADEL",
                                          "location": "Test Court",
                                          "startsAt": "2026-06-01T18:30:00",
                                          "pointsReward": 30
                                        }
                                        """)
                )
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCanReadChallenges() throws Exception {
        mockMvc.perform(get("/api/v1/challenges"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void normalUserCannotReadAdminAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadAdminAuditLogs() throws Exception {
        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isOk());
    }
}
