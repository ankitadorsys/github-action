package com.example.demo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.config.TestSecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
@Import(TestSecurityConfig.class)
@DisplayName("HelloController")
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/hello - returns default greeting without authentication")
    void hello_withDefaultName_returnsHelloWorld() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"))
                .andExpect(jsonPath("$.application").value("github-action-demo"));
    }

    @Test
    @DisplayName("GET /api/hello?name=Ankit - returns personalized greeting")
    void hello_withCustomName_returnsHelloName() throws Exception {
        mockMvc.perform(get("/api/hello").param("name", "Ankit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Ankit!"));
    }
}
