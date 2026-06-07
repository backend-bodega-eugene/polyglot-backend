package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminUserCommentService;
import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台用户评论管理接口。
 *
 * <p>提供用户评论分页查询、详情查询和后台回复能力。</p>
 */
@Tag(name = "后台用户评论管理", description = "后台用户评论分页查询、详情查询和回复接口")
@RestController
@RequestMapping("/admin/usercomments")
public class AdminUserCommentController {

    /**
     * 后台用户评论服务。
     */
    private final AdminUserCommentService adminUserCommentService;

    /**
     * 创建后台用户评论管理接口实例。
     *
     * @param adminUserCommentService 后台用户评论服务
     */
    public AdminUserCommentController(AdminUserCommentService adminUserCommentService) {
        this.adminUserCommentService = adminUserCommentService;
    }

    /**
     * 分页查询用户评论。
     *
     * @param request 用户评论分页查询条件
     * @return 用户评论分页数据
     */
    @Operation(summary = "分页查询用户评论", description = "根据分页条件和筛选条件查询用户评论列表。")
    @PostMapping("/page")
    public Result<PageResponse<UserCommentResponse>> page(
            @Parameter(description = "用户评论分页查询参数", required = true)
            @RequestBody AdminUserCommentPageRequest request) {
        return adminUserCommentService.page(request);
    }

    /**
     * 查询用户评论详情。
     *
     * @param request 用户评论详情查询参数
     * @return 用户评论详情
     */
    @Operation(summary = "查询用户评论详情", description = "根据查询参数获取用户评论详情。")
    @PostMapping("/detail")
    public Result<UserCommentResponse> detail(
            @Parameter(description = "用户评论详情查询参数", required = true)
            @RequestBody UserCommentDetailRequest request) {
        return adminUserCommentService.detail(request);
    }

    /**
     * 回复用户评论。
     *
     * @param request 用户评论回复参数
     * @return 空结果
     */
    @Operation(summary = "回复用户评论", description = "后台回复指定用户评论。")
    @PostMapping("/reply")
    public Result<Void> reply(
            @Parameter(description = "用户评论回复参数", required = true)
            @RequestBody UserCommentReplyRequest request) {
        return adminUserCommentService.reply(request);
    }
}
