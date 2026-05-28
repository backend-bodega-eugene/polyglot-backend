package com.eugene.goalhub.user.controller;


import com.eugene.goalhub.user.service.InternalAdminUserService;
import dto.*;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台管理内部用户接口。
 * <p>
 * 这些接口面向 admin-service 调用，用于在后台管理应用用户。
 */
@RestController
@RequestMapping("/internal/admin/users")
public class InternalAdminUserController {

    /**
     * 后台管理内部用户服务。
     */
    private final InternalAdminUserService internalAdminUserService;

    public InternalAdminUserController(InternalAdminUserService internalAdminUserService) {
        this.internalAdminUserService = internalAdminUserService;
    }

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页结果
     */
    @PostMapping("/page")
    public Result<PageResponse<UserAdminPageResponse>> page(@RequestBody UserAdminPageRequest request) {
        return Result.success(internalAdminUserService.page(request));
    }

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody UserAdminCreateRequest request) {
        return Result.success(internalAdminUserService.create(request));
    }

    /**
     * 更新应用用户基础信息。
     *
     * @param id      应用用户 ID
     * @param request 更新参数
     * @return 空结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
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
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
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
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable("id") Long id,
                                       @RequestBody UserAdminPasswordUpdateRequest request) {
        internalAdminUserService.updatePassword(id, request);
        return Result.success();
    }
}
