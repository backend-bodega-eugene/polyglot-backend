package com.eugene.goalhub.user.controller;


import com.eugene.goalhub.user.service.InternalAdminUserService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台管理内部用户接口。
 * <p>
 * 这些接口面向 admin-service 调用，用于在后台管理应用用户。
 */
@Tag(name = "后台内部应用用户管理", description = "面向 admin-service 的应用用户内部管理接口")
@RestController
@RequestMapping("/internal/admin/users")
public class InternalAdminUserController {

    /**
     * 后台管理内部用户服务。
     */
    private final InternalAdminUserService internalAdminUserService;

    /**
     * 创建后台管理内部用户接口。
     *
     * @param internalAdminUserService 后台管理内部用户服务
     */
    public InternalAdminUserController(InternalAdminUserService internalAdminUserService) {
        this.internalAdminUserService = internalAdminUserService;
    }

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页结果
     */
    @Operation(summary = "分页查询应用用户", description = "面向后台管理分页查询应用用户列表。")
    @PostMapping("/page")
    public Result<PageResponse<UserAdminPageResponse>> page(
            @Parameter(description = "应用用户分页查询参数", required = true)
            @RequestBody UserAdminPageRequest request) {
        return Result.success(internalAdminUserService.page(request));
    }

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    @Operation(summary = "创建应用用户", description = "面向后台管理创建新的应用用户。")
    @PostMapping
    public Result<Long> create(@Parameter(description = "应用用户创建参数", required = true)
                               @RequestBody UserAdminCreateRequest request) {
        return Result.success(internalAdminUserService.create(request));
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
        internalAdminUserService.update(id, request);
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
        internalAdminUserService.delete(id);
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
        internalAdminUserService.updatePassword(id, request);
        return Result.success();
    }
}
