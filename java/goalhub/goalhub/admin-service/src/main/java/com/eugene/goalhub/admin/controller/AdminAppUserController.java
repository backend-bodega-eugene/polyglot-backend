package com.eugene.goalhub.admin.controller;


import com.eugene.goalhub.admin.service.AdminAppUserService;
import dto.*;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台应用用户管理接口。
 */
@RestController
@RequestMapping("/admin/appusers")
public class AdminAppUserController {

    /**
     * 应用用户管理服务。
     */
    private final AdminAppUserService adminAppUserService;

    public AdminAppUserController(AdminAppUserService adminAppUserService) {
        this.adminAppUserService = adminAppUserService;
    }

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页数据
     */
    @PostMapping("/page")
    public Result<PageResponse<UserAdminPageResponse>> page(@RequestBody UserAdminPageRequest request) {
        return Result.success(adminAppUserService.page(request));
    }

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody UserAdminCreateRequest request) {
        return Result.success(adminAppUserService.create(request));
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
        adminAppUserService.update(id, request);
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
    @PutMapping("/{id}/password")
    public Result<Void> updatePassword(@PathVariable("id") Long id,
                                       @RequestBody UserAdminPasswordUpdateRequest request) {
        adminAppUserService.updatePassword(id, request);
        return Result.success();
    }
}
