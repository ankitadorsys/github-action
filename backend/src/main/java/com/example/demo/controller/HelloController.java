package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public Map<String, Object> hello(
            @RequestParam(value = "name", defaultValue = "World") String name) {
        return Map.of(
                "message", "Hello, " + name + "!",
                "timestamp", LocalDateTime.now().toString(),
                "application", "github-action-demo"
        );
    }
}
