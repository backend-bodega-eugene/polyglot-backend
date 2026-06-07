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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserCommentServiceImpl
        extends ServiceImpl<UserCommentMapper, UserCommentEntity>
        implements UserCommentService {

    @Override
    public void add(Long userId, UserCommentAddRequest request) {
        UserCommentEntity lastMessage = lambdaQuery()
                .eq(UserCommentEntity::getUserId, userId)
                .orderByDesc(UserCommentEntity::getCreatedAt)
                .last("LIMIT 1")
                .one();

        if (lastMessage != null
                && lastMessage.getCreatedAt().plusMinutes(10).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("请10分钟后再提交留言");
        }

        UserCommentEntity entity = new UserCommentEntity();
        entity.setUserId(userId);
        entity.setContact(request.getContact());
        entity.setMessage(request.getMessage());

        save(entity);
    }

    @Override
    public PageResponse<UserCommentResponse> userPage(Long userId, UserCommentPageRequest request) {
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

    @Override
    public PageResponse<UserCommentResponse> adminPage(AdminUserCommentPageRequest request) {
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

    @Override
    public UserCommentResponse detail(UserCommentDetailRequest request) {
        UserCommentEntity entity = getById(request.getId());
        if (entity == null) {
            throw new RuntimeException("留言不存在");
        }
        return toResponse(entity);
    }

    @Override
    public void reply(UserCommentReplyRequest request) {
        UserCommentEntity entity = getById(request.getId());
        if (entity == null) {
            throw new RuntimeException("留言不存在");
        }

        lambdaUpdate()
                .eq(UserCommentEntity::getId, request.getId())
                .set(UserCommentEntity::getReplyContent, request.getReplyContent())
                .set(UserCommentEntity::getReplyTime, LocalDateTime.now())
                .update();
    }

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