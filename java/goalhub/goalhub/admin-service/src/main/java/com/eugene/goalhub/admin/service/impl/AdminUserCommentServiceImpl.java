package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminUserCommentFeignClient;
import com.eugene.goalhub.admin.service.AdminUserCommentService;
import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import org.springframework.stereotype.Service;
import response.Result;

@Service
public class AdminUserCommentServiceImpl implements AdminUserCommentService {

    private final AdminUserCommentFeignClient adminUserCommentFeignClient;

    public AdminUserCommentServiceImpl(AdminUserCommentFeignClient adminUserCommentFeignClient) {
        this.adminUserCommentFeignClient = adminUserCommentFeignClient;
    }

    @Override
    public Result<PageResponse<UserCommentResponse>> page(AdminUserCommentPageRequest request) {
        return adminUserCommentFeignClient.page(request);
    }

    @Override
    public Result<UserCommentResponse> detail(UserCommentDetailRequest request) {
        return adminUserCommentFeignClient.detail(request);
    }

    @Override
    public Result<Void> reply(UserCommentReplyRequest request) {
        return adminUserCommentFeignClient.reply(request);
    }
}