package com.example.demo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloController.class)
class HelloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hello_withDefaultName_returnsHelloWorld() throws Exception {
        mockMvc.perform(get("/api/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, World!"))
                .andExpect(jsonPath("$.application").value("github-action-demo"));
    }

    @Test
    void hello_withCustomName_returnsHelloName() throws Exception {
        mockMvc.perform(get("/api/hello").param("name", "Ankit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Hello, Ankit!"));
    }
}
