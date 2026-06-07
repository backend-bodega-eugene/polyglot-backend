package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserCommentService;
import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台用户留言管理接口。
 *
 * <p>提供后台分页查询、查看详情和回复用户留言的内部接口。</p>
 */
@Tag(name = "内部后台用户留言管理", description = "面向后台管理的用户留言查询和回复接口")
@RestController
@RequestMapping("/internal/admin/usercomments")
public class InternalAdminUserCommentController {

    /**
     * 用户留言服务。
     */
    private final UserCommentService userCommentService;

    /**
     * 创建内部后台用户留言管理接口。
     *
     * @param userCommentService 用户留言服务
     */
    public InternalAdminUserCommentController(UserCommentService userCommentService) {
        this.userCommentService = userCommentService;
    }

    /**
     * 分页查询用户留言。
     *
     * @param request 用户留言分页查询参数
     * @return 用户留言分页结果
     */
    @Operation(summary = "分页查询用户留言", description = "面向后台管理分页查询用户客服留言列表。")
    @PostMapping("/page")
    public Result<PageResponse<UserCommentResponse>> page(
            @Parameter(description = "用户留言分页查询参数", required = true)
            @Valid @RequestBody AdminUserCommentPageRequest request) {
        return Result.success(userCommentService.adminPage(request));
    }

    /**
     * 查询用户留言详情。
     *
     * @param request 用户留言详情查询参数
     * @return 用户留言详情
     */
    @Operation(summary = "查询用户留言详情", description = "根据留言 ID 查询用户客服留言详情。")
    @PostMapping("/detail")
    public Result<UserCommentResponse> detail(
            @Parameter(description = "用户留言详情查询参数", required = true)
            @Valid @RequestBody UserCommentDetailRequest request) {
        return Result.success(userCommentService.detail(request));
    }

    /**
     * 回复用户留言。
     *
     * @param request 用户留言回复参数
     * @return 空结果
     */
    @Operation(summary = "回复用户留言", description = "后台管理员回复指定用户客服留言。")
    @PostMapping("/reply")
    public Result<Void> reply(
            @Parameter(description = "用户留言回复参数", required = true)
            @Valid @RequestBody UserCommentReplyRequest request) {
        userCommentService.reply(request);
        return Result.success();
    }
}
