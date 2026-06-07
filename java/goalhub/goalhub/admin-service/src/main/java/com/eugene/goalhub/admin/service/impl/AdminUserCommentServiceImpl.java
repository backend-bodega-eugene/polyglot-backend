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

/**
 * 后台用户评论管理服务实现。
 *
 * <p>当前服务通过 Feign 调用 user-service 的内部后台用户评论接口。</p>
 */
@Service
public class AdminUserCommentServiceImpl implements AdminUserCommentService {

    /**
     * 后台用户评论远程调用客户端。
     */
    private final AdminUserCommentFeignClient adminUserCommentFeignClient;

    /**
     * 创建后台用户评论管理服务实现。
     *
     * @param adminUserCommentFeignClient 后台用户评论远程调用客户端
     */
    public AdminUserCommentServiceImpl(AdminUserCommentFeignClient adminUserCommentFeignClient) {
        this.adminUserCommentFeignClient = adminUserCommentFeignClient;
    }

    /**
     * 分页查询用户评论。
     *
     * @param request 用户评论分页查询条件
     * @return 用户评论分页数据
     */
    @Override
    public Result<PageResponse<UserCommentResponse>> page(AdminUserCommentPageRequest request) {
        return adminUserCommentFeignClient.page(request);
    }

    /**
     * 查询用户评论详情。
     *
     * @param request 用户评论详情查询参数
     * @return 用户评论详情
     */
    @Override
    public Result<UserCommentResponse> detail(UserCommentDetailRequest request) {
        return adminUserCommentFeignClient.detail(request);
    }

    /**
     * 回复用户评论。
     *
     * @param request 用户评论回复参数
     * @return 空结果
     */
    @Override
    public Result<Void> reply(UserCommentReplyRequest request) {
        return adminUserCommentFeignClient.reply(request);
    }
}
