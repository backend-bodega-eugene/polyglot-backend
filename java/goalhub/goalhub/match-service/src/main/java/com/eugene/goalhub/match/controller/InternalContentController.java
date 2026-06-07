package com.eugene.goalhub.match.controller;


import com.eugene.goalhub.match.service.ContentService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部内容管理接口。
 *
 * <p>提供后台内容创建、更新、删除、详情查询和分页查询能力。</p>
 */
@Tag(name = "内部内容管理", description = "内部后台内容创建、更新、删除、详情和分页查询接口")
@RestController
@RequestMapping("/internal/contents")
public class InternalContentController {

    /**
     * 内容服务。
     */
    private final ContentService contentService;

    /**
     * 创建内部内容管理接口实例。
     *
     * @param contentService 内容服务
     */
    public InternalContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    @Operation(summary = "创建内容", description = "创建后台运营内容。")
    @PostMapping
    public Result<Long> create(
            @Parameter(description = "内容创建参数", required = true)
            @RequestBody AdminContentCreateRequest request) {
        return Result.success(contentService.create(request));
    }

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     * @return 空结果
     */
    @Operation(summary = "更新内容", description = "根据内容 ID 更新后台运营内容。")
    @PutMapping("/{id}")
    public Result<Void> update(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "内容更新参数", required = true)
            @RequestBody AdminContentUpdateRequest request) {
        contentService.update(id, request);
        return Result.success();
    }

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     * @return 空结果
     */
    @Operation(summary = "删除内容", description = "根据内容 ID 删除后台运营内容。")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id) {
        contentService.delete(id);
        return Result.success();
    }

    /**
     * 查询内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    @Operation(summary = "查询内容详情", description = "根据内容 ID 查询后台内容详情。")
    @GetMapping("/{id}")
    public Result<ContentResponse> detail(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id) {
        return Result.success(contentService.getAdminDetail(id));
    }

    /**
     * 分页查询内容。
     *
     * @param request 内容分页查询参数
     * @return 内容分页数据
     */
    @Operation(summary = "分页查询内容", description = "分页查询后台运营内容。")
    @PostMapping("/page")
    public Result<PageResponse<ContentResponse>> page(
            @Parameter(description = "内容分页查询参数", required = true)
            @RequestBody AdminContentPageRequest request) {
        return Result.success(contentService.adminPage(request));
    }
}
