package com.example.rest_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")  // This will respond to requests to the root URL
    public String hello() {
        return "Hello";
    }
}
