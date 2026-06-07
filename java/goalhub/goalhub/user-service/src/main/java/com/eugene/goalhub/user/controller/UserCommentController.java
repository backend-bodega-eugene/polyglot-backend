package com.eugene.goalhub.user.controller;

import com.eugene.goalhub.user.service.UserCommentService;
import dto.PageResponse;
import dto.UserCommentAddRequest;
import dto.UserCommentPageRequest;
import dto.UserCommentResponse;
import org.springframework.web.bind.annotation.*;
import response.Result;

@RestController
@RequestMapping("/user/usercomments")
public class UserCommentController {

    private final UserCommentService userCommentService;

    public UserCommentController(UserCommentService userCommentService) {
        this.userCommentService = userCommentService;
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestHeader("X-User-Id") Long userId,
                            @RequestBody UserCommentAddRequest request) {
        userCommentService.add(userId, request);
        return Result.success();
    }

    @PostMapping("/page")
    public Result<PageResponse<UserCommentResponse>> page(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody UserCommentPageRequest request) {
        return Result.success(userCommentService.userPage(userId, request));
    }
}