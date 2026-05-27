package com.eugene.goalhub.admin.controller;


import com.eugene.goalhub.admin.service.AdminUserService;
import dto.AdminLoginRequest;
import dto.AdminPasswordUpdateRequest;
import dto.AdminUserCreateRequest;
import dto.AdminUserUpdateRequest;
import org.springframework.web.bind.annotation.*;
import response.Result;

@RestController
@RequestMapping("/admin")
public class AdminUserController {

    private final AdminUserService adminUserService;
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
        // this.jwtUtil = jwtUtil;
    }
    @PostMapping("/auth/login")
    public Result<Object> login(@RequestBody AdminLoginRequest request) {
        return Result.success(adminUserService.login(request));
    }

    @PostMapping("/users")
    public Result<Long> create(@RequestBody AdminUserCreateRequest request) {
        return Result.success(adminUserService.create(request));
    }

    @PutMapping("/users/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody AdminUserUpdateRequest request) {
        adminUserService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return Result.success();
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> updatePassword(@PathVariable Long id,
                                       @RequestBody AdminPasswordUpdateRequest request) {
        adminUserService.updatePassword(id, request);
        return Result.success();
    }
}