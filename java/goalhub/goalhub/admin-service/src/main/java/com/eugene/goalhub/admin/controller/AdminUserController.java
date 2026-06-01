package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminUserService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台管理员账号接口。
 */
@Tag(name = "后台管理员账号", description = "后台管理员登录、分页查询、创建、更新、删除、改密和状态管理接口")
@RestController
@RequestMapping("/admin")
public class AdminUserController {

    /**
     * 管理员账号服务。
     */
    private final AdminUserService adminUserService;

    /**
     * 创建后台管理员账号接口实例。
     *
     * @param adminUserService 管理员账号服务
     */
    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * 管理员登录。
     *
     * @param request 登录参数
     * @return 登录结果
     */
    @Operation(summary = "管理员登录", description = "后台管理员使用账号密码登录，登录成功后返回 JWT 和管理员基础信息。")
    @PostMapping("/auth/login")
    public Result<Object> login(@Parameter(description = "管理员登录参数", required = true)
                                @RequestBody AdminLoginRequest request) {
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
    @Operation(summary = "分页查询管理员账号", description = "按页码、每页数量和可选用户名条件查询后台管理员账号。")
    @GetMapping("/users")
    public Result<PageResponse<AdminUserPageResponse>> page(
            @Parameter(description = "页码", required = true)
            @RequestParam("pageIndex") Integer pageIndex,
            @Parameter(description = "每页数量", required = true)
            @RequestParam("pageSize") Integer pageSize,
            @Parameter(description = "用户名筛选条件")
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
    @Operation(summary = "创建管理员账号", description = "创建一个新的后台管理员账号。")
    @PostMapping("/users")
    public Result<Long> create(@Parameter(description = "管理员创建参数", required = true)
                               @RequestBody AdminUserCreateRequest request) {
        return Result.success(adminUserService.create(request));
    }

    /**
     * 更新管理员账号基础信息。
     *
     * @param id      管理员 ID
     * @param request 更新参数
     * @return 空结果
     */
    @Operation(summary = "更新管理员账号", description = "根据管理员 ID 更新管理员基础信息。")
    @PutMapping("/users/{id}")
    public Result<Void> update(@Parameter(description = "管理员 ID", required = true)
                               @PathVariable("id") Long id,
                               @Parameter(description = "管理员更新参数", required = true)
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
    @Operation(summary = "删除管理员账号", description = "根据管理员 ID 删除后台管理员账号。")
    @DeleteMapping("/users/{id}")
    public Result<Void> delete(@Parameter(description = "管理员 ID", required = true)
                               @PathVariable("id") Long id) {
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
    @Operation(summary = "修改管理员密码", description = "根据管理员 ID 修改后台管理员登录密码。")
    @PutMapping("/users/{id}/password")
    public Result<Void> updatePassword(@Parameter(description = "管理员 ID", required = true)
                                       @PathVariable("id") Long id,
                                       @Parameter(description = "管理员密码更新参数", required = true)
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
    @Operation(summary = "更新管理员启用状态", description = "根据管理员 ID 启用或禁用后台管理员账号。")
    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "管理员 ID", required = true)
                                     @PathVariable("id") Long id,
                                     @Parameter(description = "管理员状态更新参数", required = true)
                                     @RequestBody AdminUserStatusUpdateRequest request) {
        adminUserService.updateStatus(id, request.getStatus());
        return Result.success();
    }
}
