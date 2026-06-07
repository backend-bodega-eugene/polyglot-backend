package com.eugene.goalhub.admin.client;

import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

@FeignClient(
        name = "user-service",
        contextId = "adminUserCommentFeignClient"
)
public interface AdminUserCommentFeignClient {

    @PostMapping("/internal/admin/usercomments/page")
    Result<PageResponse<UserCommentResponse>> page(
            @RequestBody AdminUserCommentPageRequest request);

    @PostMapping("/internal/admin/usercomments/detail")
    Result<UserCommentResponse> detail(
            @RequestBody UserCommentDetailRequest request);

    @PostMapping("/internal/admin/usercomments/reply")
    Result<Void> reply(
            @RequestBody UserCommentReplyRequest request);
}