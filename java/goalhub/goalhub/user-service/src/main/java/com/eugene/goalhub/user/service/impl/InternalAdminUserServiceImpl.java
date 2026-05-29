package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.mapper.UserMapper;
import com.eugene.goalhub.user.service.InternalAdminUserService;
import dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台管理内部用户服务实现。
 */
@Service
public class InternalAdminUserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements InternalAdminUserService {

    /**
     * 密码加密与校验组件。
     */
    private final PasswordEncoder passwordEncoder;
    //private final JwtUtil jwtUtil;

    public InternalAdminUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        // this.jwtUtil = jwtUtil;
    }

    /**
     * 分页查询应用用户。
     *
     * @param request 分页和筛选条件
     * @return 应用用户分页结果
     */
    @Override
    public PageResponse<UserAdminPageResponse> page(UserAdminPageRequest request) {
        Page<UserEntity> page = new Page<>(request.getPageIndex(), request.getPageSize());

        // 根据后台筛选条件动态拼接查询条件。
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<UserEntity>()
                .like(request.getUsername() != null && !request.getUsername().isBlank(),
                        UserEntity::getUsername, request.getUsername())
                .like(request.getNickname() != null && !request.getNickname().isBlank(),
                        UserEntity::getNickname, request.getNickname())
                .like(request.getEmail() != null && !request.getEmail().isBlank(),
                        UserEntity::getEmail, request.getEmail())
                .like(request.getPhone() != null && !request.getPhone().isBlank(),
                        UserEntity::getPhone, request.getPhone())
                .eq(request.getStatus() != null,
                        UserEntity::getStatus, request.getStatus())
                .ge(request.getCreatedAtStart() != null,
                        UserEntity::getCreatedAt, request.getCreatedAtStart())
                .le(request.getCreatedAtEnd() != null,
                        UserEntity::getCreatedAt, request.getCreatedAtEnd())
                .orderByDesc(UserEntity::getCreatedAt);

        Page<UserEntity> result = page(page, wrapper);

        List<UserAdminPageResponse> records = result.getRecords()
                .stream()
                .map(this::toPageResponse)
                .toList();

        PageResponse<UserAdminPageResponse> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageIndex(request.getPageIndex());
        response.setPageSize(request.getPageSize());
        response.setRecords(records);

        return response;
    }

    /**
     * 创建应用用户。
     *
     * @param request 创建参数
     * @return 新用户 ID
     */
    @Override
    public Long create(UserAdminCreateRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setStatus(request.getStatus());
        save(user);
        return user.getId();
    }

    /**
     * 更新应用用户基础信息。
     * <p>
     * 密码为空时不更新密码字段。
     *
     * @param id      应用用户 ID
     * @param request 更新参数
     */
    @Override
    public void update(Long id, UserAdminUpdateRequest request) {
        lambdaUpdate()
                .eq(UserEntity::getId, id)
                .set(UserEntity::getEmail, request.getEmail())
                .set(UserEntity::getPhone, request.getPhone())
                .set(
                        request.getPassword() != null && !request.getPassword().isBlank(),
                        UserEntity::getPasswordHash,
                        passwordEncoder.encode(request.getPassword())
                )
                .set(UserEntity::getNickname, request.getNickname())
                .set(UserEntity::getAvatarUrl, request.getAvatarUrl())
                .set(UserEntity::getStatus, request.getStatus())
                .update();
    }

    /**
     * 删除应用用户。
     *
     * @param id 应用用户 ID
     */
    @Override
    public void delete(Long id) {
        removeById(id);
    }

    /**
     * 修改应用用户密码。
     *
     * @param id      应用用户 ID
     * @param request 密码更新参数
     */
    @Override
    public void updatePassword(Long id, UserAdminPasswordUpdateRequest request) {
        lambdaUpdate()
                .eq(UserEntity::getId, id)
                .set(UserEntity::getPasswordHash, passwordEncoder.encode(request.getPassword()))
                .update();
    }

    /**
     * 将用户实体转换为后台分页响应对象。
     *
     * @param user 用户实体
     * @return 后台用户分页响应对象
     */
    private UserAdminPageResponse toPageResponse(UserEntity user) {
        UserAdminPageResponse response = new UserAdminPageResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setStatus(user.getStatus());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
