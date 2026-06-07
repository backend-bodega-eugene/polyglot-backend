package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminContentService;
import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台内容管理接口。
 *
 * <p>提供后台内容创建、更新、删除、详情查询和分页查询能力。</p>
 */
@Tag(name = "后台内容管理", description = "后台内容创建、更新、删除、详情和分页查询接口")
@RestController
@RequestMapping("/admin/contents")
public class AdminContentController {

    /**
     * 后台内容服务。
     */
    private final AdminContentService adminContentService;

    /**
     * 创建后台内容管理接口实例。
     *
     * @param adminContentService 后台内容服务
     */
    public AdminContentController(AdminContentService adminContentService) {
        this.adminContentService = adminContentService;
    }

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    @Operation(summary = "创建内容", description = "创建一条新的后台内容。")
    @PostMapping
    public Result<Long> create(
            @Parameter(description = "内容创建参数", required = true)
            @RequestBody AdminContentCreateRequest request) {
        return adminContentService.create(request);
    }

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     * @return 空结果
     */
    @Operation(summary = "更新内容", description = "根据内容 ID 更新内容信息。")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "内容更新参数", required = true)
            @RequestBody AdminContentUpdateRequest request) {
        return adminContentService.update(id, request);
    }

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     * @return 空结果
     */
    @Operation(summary = "删除内容", description = "根据内容 ID 删除内容。")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id) {
        return adminContentService.delete(id);
    }

    /**
     * 查询内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    @Operation(summary = "查询内容详情", description = "根据内容 ID 查询内容详情。")
    @GetMapping("/{id}")
    public Result<ContentResponse> detail(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id) {
        return adminContentService.detail(id);
    }

    /**
     * 分页查询内容。
     *
     * @param request 内容分页查询条件
     * @return 内容分页数据
     */
    @Operation(summary = "分页查询内容", description = "根据分页条件和筛选条件查询内容列表。")
    @PostMapping("/page")
    public Result<PageResponse<ContentResponse>> page(
            @Parameter(description = "内容分页查询参数", required = true)
            @RequestBody AdminContentPageRequest request) {
        return adminContentService.page(request);
    }
}
