package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminUserMenuService;
import dto.AdminMenuTreeResponse;
import dto.AdminUserMenuSaveRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 管理员菜单权限接口。
 */
@Tag(name = "后台管理员菜单权限", description = "后台管理员菜单权限查询和保存接口")
@RestController
@RequestMapping("/admin")
public class AdminUserMenuController {

    /**
     * 管理员菜单权限服务。
     */
    private final AdminUserMenuService adminUserMenuService;

    /**
     * 创建管理员菜单权限接口实例。
     *
     * @param adminUserMenuService 管理员菜单权限服务
     */
    public AdminUserMenuController(AdminUserMenuService adminUserMenuService) {
        this.adminUserMenuService = adminUserMenuService;
    }

    /**
     * 查询指定管理员拥有的菜单权限。
     *
     * @param id 管理员 ID
     * @return 菜单树列表
     */
    @Operation(summary = "查询管理员菜单权限", description = "根据管理员 ID 查询该管理员拥有的菜单权限。")
    @GetMapping("/users/{id}/menus")
    public Result<List<AdminMenuTreeResponse>> getUserMenus(
            @Parameter(description = "管理员 ID", required = true)
            @PathVariable("id") Long id) {
        return Result.success(adminUserMenuService.getUserMenus(id));
    }

    /**
     * 保存指定管理员的菜单权限。
     *
     * @param id      管理员 ID
     * @param request 菜单 ID 列表
     * @return 空结果
     */
    @Operation(summary = "保存管理员菜单权限", description = "根据管理员 ID 保存该管理员可访问的菜单 ID 列表。")
    @PutMapping("/users/{id}/menus")
    public Result<Void> saveUserMenus(@Parameter(description = "管理员 ID", required = true)
                                      @PathVariable("id") Long id,
                                      @Parameter(description = "菜单权限保存参数", required = true)
                                      @RequestBody AdminUserMenuSaveRequest request) {
        adminUserMenuService.saveUserMenus(id, request.getMenuIds());
        return Result.success();
    }

    /**
     * 查询当前登录管理员可访问的菜单。
     *
     * @param adminUserId 当前管理员 ID
     * @param username    当前管理员用户名
     * @return 菜单树列表
     */
    @Operation(summary = "查询当前登录管理员菜单", description = "根据网关透传的管理员身份请求头查询当前登录管理员可访问的菜单。")
    @GetMapping("/auth/menus")
    public Result<List<AdminMenuTreeResponse>> getCurrentLoginMenus(
            @Parameter(description = "当前管理员 ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminUserId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username
    ) {
        return Result.success(adminUserMenuService.getCurrentLoginMenus(adminUserId, username));
    }
}
