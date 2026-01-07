package com.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class LoginController {

    private static final Set<String> ALLOWED = Set.of("老胡", "雪梅");

    @GetMapping("/login")
    public String login(@RequestParam String name) {
        return ALLOWED.contains(name) ? "ok" : "no";
    }
}
