package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMenuService;
import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

@RestController
@RequestMapping("/admin/menus")
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    @GetMapping
    public Result<List<AdminMenuTreeResponse>> tree() {
        return Result.success(adminMenuService.tree());
    }

    @PostMapping
    public Result<Long> create(@RequestBody AdminMenuCreateRequest request) {
        return Result.success(adminMenuService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody AdminMenuUpdateRequest request) {
        adminMenuService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminMenuService.delete(id);
        return Result.success();
    }
}