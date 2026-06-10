package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserService;
import dto.UserProfileResponse;
import dto.UserProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

@Tag(name = "用户资料", description = "当前登录用户资料查询和修改")
@RestController
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "获取当前用户资料")
    @GetMapping("/me")
    public Result<UserProfileResponse> me(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        return Result.success(userService.getProfile(userId));
    }

    @Operation(summary = "修改当前用户资料")
    @PutMapping("/me")
    public Result<Void> updateProfile(
            @Parameter(description = "当前登录用户 ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        userService.updateProfile(userId, request);
        return Result.success();
    }
}