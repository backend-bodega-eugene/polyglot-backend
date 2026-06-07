package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserCommentService;
import dto.PageResponse;
import dto.UserCommentAddRequest;
import dto.UserCommentPageRequest;
import dto.UserCommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端用户留言接口。
 *
 * <p>提供当前登录用户提交客服留言和分页查询本人留言的接口。</p>
 */
@Tag(name = "前端用户留言", description = "前端用户客服留言提交和查询接口")
@RestController
@RequestMapping("/user/usercomments")
public class UserCommentController {

    /**
     * 用户留言服务。
     */
    private final UserCommentService userCommentService;

    /**
     * 创建前端用户留言接口。
     *
     * @param userCommentService 用户留言服务
     */
    public UserCommentController(UserCommentService userCommentService) {
        this.userCommentService = userCommentService;
    }

    /**
     * 提交用户留言。
     *
     * @param userId  当前登录用户 ID
     * @param request 用户留言提交参数
     * @return 空结果
     */
    @Operation(summary = "提交用户留言", description = "当前登录用户提交客服留言。")
    @PostMapping("/add")
    public Result<Void> add(@Parameter(description = "当前登录用户ID", required = true)
                            @RequestHeader("X-User-Id") Long userId,
                            @Parameter(description = "用户留言提交参数", required = true)
                            @Valid @RequestBody UserCommentAddRequest request) {
        userCommentService.add(userId, request);
        return Result.success();
    }

    /**
     * 分页查询当前用户留言。
     *
     * @param userId  当前登录用户 ID
     * @param request 用户留言分页查询参数
     * @return 用户留言分页结果
     */
    @Operation(summary = "查询我的留言", description = "分页查询当前登录用户提交过的客服留言。")
    @PostMapping("/page")
    public Result<PageResponse<UserCommentResponse>> page(
            @Parameter(description = "当前登录用户ID", required = true)
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "用户留言分页查询参数", required = true)
            @Valid @RequestBody UserCommentPageRequest request) {
        return Result.success(userCommentService.userPage(userId, request));
    }
}
