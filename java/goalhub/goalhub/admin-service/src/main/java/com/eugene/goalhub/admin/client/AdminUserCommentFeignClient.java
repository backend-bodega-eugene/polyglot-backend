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

/**
 * user-service 内部管理端用户评论 Feign 客户端。
 */
@FeignClient(
        name = "user-service",
        contextId = "adminUserCommentFeignClient"
)
public interface AdminUserCommentFeignClient {

    /**
     * 分页查询用户评论。
     *
     * @param request 用户评论分页查询条件
     * @return 用户评论分页数据
     */
    @PostMapping("/internal/admin/usercomments/page")
    Result<PageResponse<UserCommentResponse>> page(
            @RequestBody AdminUserCommentPageRequest request);

    /**
     * 查询用户评论详情。
     *
     * @param request 用户评论详情查询参数
     * @return 用户评论详情
     */
    @PostMapping("/internal/admin/usercomments/detail")
    Result<UserCommentResponse> detail(
            @RequestBody UserCommentDetailRequest request);

    /**
     * 回复用户评论。
     *
     * @param request 用户评论回复参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/usercomments/reply")
    Result<Void> reply(
            @RequestBody UserCommentReplyRequest request);
}
