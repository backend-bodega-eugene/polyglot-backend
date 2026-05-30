package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMenuService;
import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 后台菜单管理接口。
 */
@RestController
@RequestMapping("/admin/menus")
public class AdminMenuController {

    /**
     * 菜单业务服务。
     */
    private final AdminMenuService adminMenuService;

    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    /**
     * 查询后台菜单树。
     *
     * @return 菜单树列表
     */
    @GetMapping
    public Result<List<AdminMenuTreeResponse>> tree() {
        return Result.success(adminMenuService.tree());
    }

    /**
     * 创建后台菜单。
     *
     * @param request 菜单创建参数
     * @return 新菜单 ID
     */
    @PostMapping
    public Result<Long> create(@RequestBody AdminMenuCreateRequest request) {
        return Result.success(adminMenuService.create(request));
    }

    /**
     * 更新后台菜单。
     *
     * @param id      菜单 ID
     * @param request 菜单更新参数
     * @return 空结果
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable("id") Long id,
                               @RequestBody AdminMenuUpdateRequest request) {
        adminMenuService.update(id, request);
        return Result.success();
    }

    /**
     * 删除后台菜单。
     *
     * @param id 菜单 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        adminMenuService.delete(id);
        return Result.success();
    }
}
