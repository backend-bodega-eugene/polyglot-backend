package com.eugene.goalhub.admin.service;

import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentDetailRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import response.Result;

/**
 * 后台用户评论管理服务。
 */
public interface AdminUserCommentService {

    /**
     * 分页查询用户评论。
     *
     * @param request 用户评论分页查询条件
     * @return 用户评论分页数据
     */
    Result<PageResponse<UserCommentResponse>> page(AdminUserCommentPageRequest request);

    /**
     * 查询用户评论详情。
     *
     * @param request 用户评论详情查询参数
     * @return 用户评论详情
     */
    Result<UserCommentResponse> detail(UserCommentDetailRequest request);

    /**
     * 回复用户评论。
     *
     * @param request 用户评论回复参数
     * @return 空结果
     */
    Result<Void> reply(UserCommentReplyRequest request);
}
