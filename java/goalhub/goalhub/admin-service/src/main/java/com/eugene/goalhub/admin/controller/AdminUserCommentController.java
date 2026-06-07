package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminUserCommentService;
import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import org.springframework.web.bind.annotation.*;
import response.Result;

@RestController
@RequestMapping("/admin/usercomments")
public class AdminUserCommentController {

    private final AdminUserCommentService adminUserCommentService;

    public AdminUserCommentController(AdminUserCommentService adminUserCommentService) {
        this.adminUserCommentService = adminUserCommentService;
    }

    @PostMapping("/page")
    public Result<PageResponse<UserCommentResponse>> page(
            @RequestBody AdminUserCommentPageRequest request) {
        return adminUserCommentService.page(request);
    }

    @PostMapping("/detail")
    public Result<UserCommentResponse> detail(
            @RequestBody UserCommentDetailRequest request) {
        return adminUserCommentService.detail(request);
    }

    @PostMapping("/reply")
    public Result<Void> reply(
            @RequestBody UserCommentReplyRequest request) {
        return adminUserCommentService.reply(request);
    }
}