package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMenuService;
import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 后台菜单管理接口。
 *
 * <p>维护后台系统菜单树结构，包含菜单查询、创建、更新和删除。</p>
 */
@Tag(name = "后台菜单管理", description = "后台菜单树查询、创建、更新和删除接口")
@RestController
@RequestMapping("/admin/menus")
public class AdminMenuController {

    /**
     * 菜单业务服务。
     */
    private final AdminMenuService adminMenuService;

    /**
     * 创建后台菜单管理接口实例。
     *
     * @param adminMenuService 菜单业务服务
     */
    public AdminMenuController(AdminMenuService adminMenuService) {
        this.adminMenuService = adminMenuService;
    }

    /**
     * 查询后台菜单树。
     *
     * @return 菜单树列表
     */
    @Operation(summary = "查询后台菜单树", description = "查询后台系统的菜单树结构。")
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
    @Operation(summary = "创建后台菜单", description = "创建一个新的后台菜单节点。")
    @PostMapping
    public Result<Long> create(@Parameter(description = "菜单创建参数", required = true)
                               @Valid @RequestBody AdminMenuCreateRequest request) {
        return Result.success(adminMenuService.create(request));
    }

    /**
     * 更新后台菜单。
     *
     * @param id      菜单 ID
     * @param request 菜单更新参数
     * @return 空结果
     */
    @Operation(summary = "更新后台菜单", description = "根据菜单 ID 更新后台菜单信息。")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "菜单 ID", required = true)
                               @PathVariable("id") Long id,
                               @Parameter(description = "菜单更新参数", required = true)
                               @Valid @RequestBody AdminMenuUpdateRequest request) {
        adminMenuService.update(id, request);
        return Result.success();
    }

    /**
     * 删除后台菜单。
     *
     * @param id 菜单 ID
     * @return 空结果
     */
    @Operation(summary = "删除后台菜单", description = "根据菜单 ID 删除后台菜单。")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "菜单 ID", required = true)
                               @PathVariable("id") Long id) {
        adminMenuService.delete(id);
        return Result.success();
    }
}
