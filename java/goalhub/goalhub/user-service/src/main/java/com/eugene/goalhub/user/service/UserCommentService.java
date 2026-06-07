package com.eugene.goalhub.user.service;

import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentAddRequest;
import dto.UserCommentDetailRequest;
import dto.UserCommentPageRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;

public interface UserCommentService {

    void add(Long userId, UserCommentAddRequest request);

    PageResponse<UserCommentResponse> userPage(Long userId, UserCommentPageRequest request);

    PageResponse<UserCommentResponse> adminPage(AdminUserCommentPageRequest request);

    UserCommentResponse detail(UserCommentDetailRequest request);

    void reply(UserCommentReplyRequest request);
}