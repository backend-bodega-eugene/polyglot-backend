package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserCommentService;
import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import org.springframework.web.bind.annotation.*;
import response.Result;

@RestController
@RequestMapping("/internal/admin/usercomments")
public class InternalAdminUserCommentController {

    private final UserCommentService userCommentService;

    public InternalAdminUserCommentController(UserCommentService userCommentService) {
        this.userCommentService = userCommentService;
    }

    @PostMapping("/page")
    public Result<PageResponse<UserCommentResponse>> page(
            @RequestBody AdminUserCommentPageRequest request) {
        return Result.success(userCommentService.adminPage(request));
    }

    @PostMapping("/detail")
    public Result<UserCommentResponse> detail(
            @RequestBody UserCommentDetailRequest request) {
        return Result.success(userCommentService.detail(request));
    }

    @PostMapping("/reply")
    public Result<Void> reply(
            @RequestBody UserCommentReplyRequest request) {
        userCommentService.reply(request);
        return Result.success();
    }
}