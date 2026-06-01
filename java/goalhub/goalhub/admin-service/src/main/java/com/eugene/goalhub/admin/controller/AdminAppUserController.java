package com.eugene.goalhub.admin.controller;


import com.eugene.goalhub.admin.service.AdminAppUserService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台应用用户管理接口。
 */
@Tag(name = "后台应用用户管理", description = "后台应用用户分页查询、创建、更新、删除和改密接口")
@RestController
@RequestMapping("/admin/appusers")
public class AdminAppUserController {

    /**
     * 应用用户管理服务。
     */
    private final AdminAppUserService adminAppUserService;

    /**
     * 创建后台应用用户管理接口实例。
     *
     * @param adminAppUserService 应用用户管理服务
     */
    public AdminAppUserController(AdminAppUserService adminAppUserService) {
        this.adminAppUserService = adminAppUserService;
    }

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页数据
     */
    @Operation(summary = "分页查询应用用户", description = "根据分页参数和筛选条件查询应用用户列表。")
    @PostMapping("/page")
    public Result<PageResponse<UserAdminPageResponse>> page(
            @Parameter(description = "应用用户分页查询参数", required = true)
            @RequestBody UserAdminPageRequest request) {
        return Result.success(adminAppUserService.page(request));
    }

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    @Operation(summary = "创建应用用户", description = "在后台创建一个新的应用用户。")
    @PostMapping
    public Result<Long> create(@Parameter(description = "应用用户创建参数", required = true)
                               @RequestBody UserAdminCreateRequest request) {
        return Result.success(adminAppUserService.create(request));
    }

    /**
     * 更新应用用户基础信息。
     *
     * @param id      应用用户 ID
     * @param request 更新参数
     * @return 空结果
     */
    @Operation(summary = "更新应用用户", description = "根据应用用户 ID 更新用户基础信息。")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "应用用户 ID", required = true)
                               @PathVariable("id") Long id,
                               @Parameter(description = "应用用户更新参数", required = true)
                               @RequestBody UserAdminUpdateRequest request) {
        adminAppUserService.update(id, request);
        return Result.success();
    }

    /**
     * 删除应用用户。
     *
     * @param id 应用用户 ID
     * @return 空结果
     */
    @Operation(summary = "删除应用用户", description = "根据应用用户 ID 删除应用用户。")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "应用用户 ID", required = true)
                               @PathVariable("id") Long id) {
        adminAppUserService.delete(id);
        return Result.success();
    }

    /**
     * 修改应用用户密码。
     *
     * @param id      应用用户 ID
     * @param request 密码更新参数
     * @return 空结果
     */
    @Operation(summary = "修改应用用户密码", description = "根据应用用户 ID 修改应用用户登录密码。")
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@Parameter(description = "应用用户 ID", required = true)
                                       @PathVariable("id") Long id,
                                       @Parameter(description = "应用用户密码更新参数", required = true)
                                       @RequestBody UserAdminPasswordUpdateRequest request) {
        adminAppUserService.updatePassword(id, request);
        return Result.success();
    }
}
