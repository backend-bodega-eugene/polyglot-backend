package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminUserService;
import dto.*;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台管理员账号接口。
 */
@RestController
@RequestMapping("/admin")
public class AdminUserController {

    /**
     * 管理员账号服务。
     */
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 管理员登录。
     *
     * @param request 登录参数
     * @return 登录结果
     */
    @PostMapping("/auth/login")
    public Result<Object> login(@RequestBody AdminLoginRequest request) {
        return Result.success(adminUserService.login(request));
    }

    /**
     * 分页查询管理员账号。
     *
     * @param pageIndex 页码
     * @param pageSize  每页数量
     * @param username  用户名筛选条件
     * @return 管理员分页数据
     */
    @GetMapping("/users")
    public Result<PageResponse<AdminUserPageResponse>> page(
            @RequestParam("pageIndex") Integer pageIndex,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam(value = "username", required = false) String username
    ) {
        return Result.success(adminUserService.page(pageIndex, pageSize, username));
    }

    /**
     * 创建管理员账号。
     *
     * @param request 创建参数
     * @return 新管理员 ID
     */
    @PostMapping("/users")
    public Result<Long> create(@RequestBody AdminUserCreateRequest request) {
        return Result.success(adminUserService.create(request));
    }

    /**
     * 更新管理员账号基础信息。
     *
     * @param id      管理员 ID
     * @param request 更新参数
     * @return 空结果
     */
    @PutMapping("/users/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody AdminUserUpdateRequest request) {
        adminUserService.update(id, request);
        return Result.success();
    }

    /**
     * 删除管理员账号。
     *
     * @param id 管理员 ID
     * @return 空结果
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        adminUserService.delete(id);
        return Result.success();
    }

    /**
     * 修改管理员密码。
     *
     * @param id      管理员 ID
     * @param request 密码更新参数
     * @return 空结果
     */
    @PutMapping("/users/{id}/password")
    public Result<Void> updatePassword(@PathVariable("id") Long id,
                                       @RequestBody AdminPasswordUpdateRequest request) {
        adminUserService.updatePassword(id, request);
        return Result.success();
    }

    /**
     * 更新管理员启用状态。
     *
     * @param id      管理员 ID
     * @param request 状态更新参数
     * @return 空结果
     */
    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id,
                                     @RequestBody AdminUserStatusUpdateRequest request) {
        adminUserService.updateStatus(id, request.getStatus());
        return Result.success();
    }
}
