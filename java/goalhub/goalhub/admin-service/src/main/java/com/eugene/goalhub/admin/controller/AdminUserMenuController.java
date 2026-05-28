package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminUserMenuService;
import dto.AdminMenuTreeResponse;
import dto.AdminUserMenuSaveRequest;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 管理员菜单权限接口。
 */
@RestController
@RequestMapping("/admin")
public class AdminUserMenuController {

    /**
     * 管理员菜单权限服务。
     */
    private final AdminUserMenuService adminUserMenuService;

    public AdminUserMenuController(AdminUserMenuService adminUserMenuService) {
        this.adminUserMenuService = adminUserMenuService;
    }

    /**
     * 查询指定管理员拥有的菜单权限。
     *
     * @param id 管理员 ID
     * @return 菜单树列表
     */
    @GetMapping("/users/{id}/menus")
    public Result<List<AdminMenuTreeResponse>> getUserMenus(@PathVariable("id") Long id) {
        return Result.success(adminUserMenuService.getUserMenus(id));
    }

    /**
     * 保存指定管理员的菜单权限。
     *
     * @param id      管理员 ID
     * @param request 菜单 ID 列表
     * @return 空结果
     */
    @PutMapping("/users/{id}/menus")
    public Result<Void> saveUserMenus(@PathVariable("id") Long id,
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
    @GetMapping("/auth/menus")
    public Result<List<AdminMenuTreeResponse>> getCurrentLoginMenus(
            @RequestParam("adminUserId") Long adminUserId,
            @RequestParam("username") String username
    ) {
        return Result.success(adminUserMenuService.getCurrentLoginMenus(adminUserId, username));
    }
}
