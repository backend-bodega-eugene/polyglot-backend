package com.eugene.goalhub.user.service;

import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentAddRequest;
import dto.UserCommentDetailRequest;
import dto.UserCommentPageRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;

/**
 * 用户留言服务。
 *
 * <p>定义前端用户留言提交、分页查询，以及后台留言查询和回复能力。</p>
 */
public interface UserCommentService {

    /**
     * 新增用户留言。
     *
     * @param userId  用户 ID
     * @param request 用户留言提交参数
     */
    void add(Long userId, UserCommentAddRequest request);

    /**
     * 分页查询当前用户留言。
     *
     * @param userId  用户 ID
     * @param request 用户留言分页查询参数
     * @return 用户留言分页结果
     */
    PageResponse<UserCommentResponse> userPage(Long userId, UserCommentPageRequest request);

    /**
     * 后台分页查询用户留言。
     *
     * @param request 后台用户留言分页查询参数
     * @return 用户留言分页结果
     */
    PageResponse<UserCommentResponse> adminPage(AdminUserCommentPageRequest request);

    /**
     * 查询用户留言详情。
     *
     * @param request 用户留言详情查询参数
     * @return 用户留言详情
     */
    UserCommentResponse detail(UserCommentDetailRequest request);

    /**
     * 回复用户留言。
     *
     * @param request 用户留言回复参数
     */
    void reply(UserCommentReplyRequest request);
}
