package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserTestController {

    private final UserService userService;

    public UserTestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users/db-test")
    public Object dbTest() {
        return userService.getById(1L);
    }
}