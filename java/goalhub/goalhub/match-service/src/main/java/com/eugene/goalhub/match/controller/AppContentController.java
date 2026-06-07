package com.eugene.goalhub.match.controller;


import com.eugene.goalhub.match.service.ContentService;
import dto.AppContentPageRequest;
import dto.ContentResponse;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

/**
 * App 内容接口。
 *
 * <p>提供 App 端文章、消息、让球教程和内容详情查询能力。</p>
 */
@Tag(name = "App 内容", description = "App 文章、消息、让球教程和内容详情接口")
@RestController
@RequestMapping("/soccer/contents")
public class AppContentController {

    /**
     * 内容服务。
     */
    private final ContentService contentService;

    /**
     * 创建 App 内容接口实例。
     *
     * @param contentService 内容服务
     */
    public AppContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    /**
     * 查询让球教程。
     *
     * @return 让球教程内容
     */
    @Operation(summary = "查询让球教程", description = "查询 App 端展示的让球教程内容。")
    @GetMapping("/articles/handicaptutorial")
    public Result<ContentResponse> handicapTutorial() {
        return Result.success(contentService.getHandicapTutorial());
    }

    /**
     * 分页查询文章。
     *
     * @param request 内容分页查询参数
     * @return 文章分页数据
     */
    @Operation(summary = "分页查询文章", description = "分页查询 App 端文章内容。")
    @GetMapping("/articles")
    public Result<PageResponse<ContentResponse>> articles(
            @Parameter(description = "内容分页查询参数")
            AppContentPageRequest request) {
        return Result.success(contentService.appArticlePage(request));
    }

    /**
     * 分页查询消息。
     *
     * @param request 内容分页查询参数
     * @return 消息分页数据
     */
    @Operation(summary = "分页查询消息", description = "分页查询 App 端消息内容。")
    @GetMapping("/messages")
    public Result<PageResponse<ContentResponse>> messages(
            @Parameter(description = "内容分页查询参数")
            AppContentPageRequest request) {
        return Result.success(contentService.appMessagePage(request));
    }

    /**
     * 查询内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    @Operation(summary = "查询内容详情", description = "根据内容 ID 查询 App 端内容详情。")
    @GetMapping("/{id}")
    public Result<ContentResponse> detail(
            @Parameter(description = "内容 ID", required = true)
            @PathVariable("id") Long id) {
        return Result.success(contentService.getAppDetail(id));
    }
}
