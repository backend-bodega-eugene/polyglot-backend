package com.eugene.goalhub.admin.service;

import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import response.Result;

public interface AdminUserCommentService {

    Result<PageResponse<UserCommentResponse>> page(AdminUserCommentPageRequest request);

    Result<UserCommentResponse> detail(UserCommentDetailRequest request);

    Result<Void> reply(UserCommentReplyRequest request);
}