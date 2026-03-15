package com.example.demo.controller;

import com.example.demo.security.AuthenticatedUser;
import com.example.demo.security.AuthenticationService;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Tests - Authentication & Authorization")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Nested
    @DisplayName("Unauthenticated requests (no JWT)")
    class UnauthenticatedTests {

        @Test
        @DisplayName("GET /api/hello - public endpoint returns 200 without auth")
        void hello_isPublic() throws Exception {
            mockMvc.perform(get("/api/hello"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/tasks - returns 401 without JWT")
        void getTasks_returns401() throws Exception {
            mockMvc.perform(get("/api/tasks"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /api/tasks - returns 401 without JWT")
        void createTask_returns401() throws Exception {
            mockMvc.perform(post("/api/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"Test\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/categories - returns 401 without JWT")
        void getCategories_returns401() throws Exception {
            mockMvc.perform(get("/api/categories"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/tags - returns 401 without JWT")
        void getTags_returns401() throws Exception {
            mockMvc.perform(get("/api/tags"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/tasks/1/comments - returns 401 without JWT")
        void getComments_returns401() throws Exception {
            mockMvc.perform(get("/api/tasks/1/comments"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /actuator/health - public endpoint returns 200 without auth")
        void actuatorHealth_isPublic() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Forbidden requests (wrong role)")
    class ForbiddenTests {

        @Test
        @DisplayName("POST /api/categories - USER role returns 403")
        void createCategory_asUser_returns403() throws Exception {
            mockMvc.perform(post("/api/categories")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Work\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/categories/1 - USER role returns 403")
        void updateCategory_asUser_returns403() throws Exception {
            mockMvc.perform(put("/api/categories/1")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Work\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/categories/1 - USER role returns 403")
        void deleteCategory_asUser_returns403() throws Exception {
            mockMvc.perform(delete("/api/categories/1")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("POST /api/tags - USER role returns 403")
        void createTag_asUser_returns403() throws Exception {
            mockMvc.perform(post("/api/tags")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"urgent\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("PUT /api/tags/1 - USER role returns 403")
        void updateTag_asUser_returns403() throws Exception {
            mockMvc.perform(put("/api/tags/1")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"urgent\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/tags/1 - USER role returns 403")
        void deleteTag_asUser_returns403() throws Exception {
            mockMvc.perform(delete("/api/tags/1")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("GET /api/tasks - no recognized role returns 403")
        void getTasks_noRole_returns403() throws Exception {
            mockMvc.perform(get("/api/tasks")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_UNKNOWN"))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Authorized requests (correct role)")
    class AuthorizedTests {

        @Test
        @DisplayName("GET /api/categories - USER role can read categories")
        void getCategories_asUser_returns200() throws Exception {
            mockMvc.perform(get("/api/categories")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/tags - USER role can read tags")
        void getTags_asUser_returns200() throws Exception {
            mockMvc.perform(get("/api/tags")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/tasks - USER role can list tasks")
        void getTasks_asUser_returns200() throws Exception {
            given(authenticationService.getCurrentUser())
                    .willReturn(new AuthenticatedUser("user1-uuid", "user1", Set.of("ROLE_USER")));

            mockMvc.perform(get("/api/tasks")
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/tasks - ADMIN role can list tasks")
        void getTasks_asAdmin_returns200() throws Exception {
            given(authenticationService.getCurrentUser())
                    .willReturn(new AuthenticatedUser("admin-uuid", "admin", Set.of("ROLE_USER", "ROLE_ADMIN")));

            mockMvc.perform(get("/api/tasks")
                            .with(jwt().authorities(
                                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                                    new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isOk());
        }
    }
}
