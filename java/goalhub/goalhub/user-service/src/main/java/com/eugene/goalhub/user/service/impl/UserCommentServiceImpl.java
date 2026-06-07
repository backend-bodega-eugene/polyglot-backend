package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.user.entity.UserCommentEntity;
import com.eugene.goalhub.user.mapper.UserCommentMapper;
import com.eugene.goalhub.user.service.UserCommentService;
import dto.AdminUserCommentPageRequest;
import dto.PageResponse;
import dto.UserCommentAddRequest;
import dto.UserCommentDetailRequest;
import dto.UserCommentPageRequest;
import dto.UserCommentReplyRequest;
import dto.UserCommentResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.time.LocalDateTime;

/**
 * 用户留言服务实现。
 *
 * <p>负责前端用户提交客服留言、查询已回复留言，以及后台查询和回复留言。</p>
 */
@Service
public class UserCommentServiceImpl
        extends ServiceImpl<UserCommentMapper, UserCommentEntity>
        implements UserCommentService {

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 新增用户留言。
     *
     * <p>同一用户最近一次留言未超过 10 分钟时不允许重复提交。</p>
     *
     * @param userId  用户 ID
     * @param request 用户留言提交参数
     */
    @Override
    public void add(Long userId, UserCommentAddRequest request) {
        UserCommentEntity lastMessage = lambdaQuery()
                .eq(UserCommentEntity::getUserId, userId)
                .orderByDesc(UserCommentEntity::getCreatedAt)
                .last("LIMIT 1")
                .one();

        if (lastMessage != null
                && lastMessage.getCreatedAt().plusMinutes(10).isAfter(LocalDateTime.now())) {
            throw new BusinessException(ResultCode.USER_COMMENT_TOO_FREQUENT);
        }

        UserCommentEntity entity = new UserCommentEntity();
        entity.setUserId(userId);
        entity.setContact(request.getContact());
        entity.setMessage(request.getMessage());

        save(entity);
    }

    /**
     * 分页查询当前用户已回复留言。
     *
     * @param userId  用户 ID
     * @param request 用户留言分页查询参数
     * @return 当前用户留言分页结果
     */
    @Override
    public PageResponse<UserCommentResponse> userPage(Long userId, UserCommentPageRequest request) {
        if (request == null) {
            request = new UserCommentPageRequest();
        }

        initPage(request);

        Page<UserCommentEntity> page = lambdaQuery()
                .eq(UserCommentEntity::getUserId, userId)
                .isNotNull(UserCommentEntity::getReplyContent)
                .orderByDesc(UserCommentEntity::getReplyTime)
                .orderByDesc(UserCommentEntity::getId)
                .page(new Page<>(request.getPageIndex(), request.getPageSize()));

        return new PageResponse<>(
                page.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                page.getRecords().stream().map(this::toResponse).toList()
        );
    }

    /**
     * 后台分页查询用户留言。
     *
     * @param request 后台用户留言分页查询参数
     * @return 用户留言分页结果
     */
    @Override
    public PageResponse<UserCommentResponse> adminPage(AdminUserCommentPageRequest request) {
        if (request == null) {
            request = new AdminUserCommentPageRequest();
        }

        initPage(request);

        Page<UserCommentEntity> page = lambdaQuery()
                .eq(request.getUserId() != null, UserCommentEntity::getUserId, request.getUserId())
                .orderByDesc(UserCommentEntity::getCreatedAt)
                .orderByDesc(UserCommentEntity::getId)
                .page(new Page<>(request.getPageIndex(), request.getPageSize()));

        return new PageResponse<>(
                page.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                page.getRecords().stream().map(this::toResponse).toList()
        );
    }

    /**
     * 查询用户留言详情。
     *
     * @param request 用户留言详情查询参数
     * @return 用户留言详情
     */
    @Override
    public UserCommentResponse detail(UserCommentDetailRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        UserCommentEntity entity = getById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.USER_COMMENT_NOT_FOUND);
        }
        return toResponse(entity);
    }

    /**
     * 回复用户留言。
     *
     * @param request 用户留言回复参数
     */
    @Override
    public void reply(UserCommentReplyRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        UserCommentEntity entity = getById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.USER_COMMENT_NOT_FOUND);
        }

        boolean updated = lambdaUpdate()
                .eq(UserCommentEntity::getId, request.getId())
                .set(UserCommentEntity::getReplyContent, request.getReplyContent())
                .set(UserCommentEntity::getReplyTime, LocalDateTime.now())
                .update();

        if (!updated) {
            throw new BusinessException(ResultCode.USER_COMMENT_NOT_FOUND);
        }
    }

    /**
     * 初始化用户留言分页参数。
     *
     * @param request 用户留言分页查询参数
     */
    private void initPage(UserCommentPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 初始化后台用户留言分页参数。
     *
     * @param request 后台用户留言分页查询参数
     */
    private void initPage(AdminUserCommentPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 转换用户留言响应。
     *
     * @param entity 用户留言实体
     * @return 用户留言响应
     */
    private UserCommentResponse toResponse(UserCommentEntity entity) {
        UserCommentResponse response = new UserCommentResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setContact(entity.getContact());
        response.setMessage(entity.getMessage());
        response.setReplyContent(entity.getReplyContent());
        response.setReplyTime(entity.getReplyTime());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
