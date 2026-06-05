package com.eugene.goalhub.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.user.entity.UserEntity;
import com.eugene.goalhub.user.mapper.UserMapper;
import com.eugene.goalhub.user.service.InternalAdminUserService;
import dto.*;
import exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 后台管理内部用户服务实现。
 *
 * <p>负责 admin-service 对前端应用用户的分页查询、创建、更新、删除和密码修改。</p>
 */
@Service
public class InternalAdminUserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements InternalAdminUserService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "后台应用用户管理";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 密码加密与校验组件。
     */
    private final PasswordEncoder passwordEncoder;
    //private final JwtUtil jwtUtil;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建后台管理内部用户服务实现。
     *
     * @param passwordEncoder  密码加密与校验组件
     * @param goalhubLogService 日志写入服务
     */
    public InternalAdminUserServiceImpl(PasswordEncoder passwordEncoder,
                                        GoalhubLogService goalhubLogService) {
        this.passwordEncoder = passwordEncoder;
        this.goalhubLogService = goalhubLogService;
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
        if (request == null) {
            request = new UserAdminPageRequest();
        }
        initPage(request);

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

        goalhubLogService.sysLog(
                MODULE_NAME,
                "USER_PAGE",
                "分页查询应用用户，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", username=" + request.getUsername()
                        + ", status=" + request.getStatus()
                        + ", total=" + result.getTotal()
        );
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
        validateStatus(request.getStatus());
        checkUsernameUnique(request.getUsername(), null);
        checkEmailUnique(request.getEmail(), null);
        checkPhoneUnique(request.getPhone(), null);

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setStatus(request.getStatus());
        save(user);
        goalhubLogService.bizLog(
                MODULE_NAME,
                "CREATE_APP_USER",
                null,
                null,
                "创建应用用户成功，userId=" + user.getId() + ", username=" + user.getUsername()
        );
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
        checkUserExists(id);
        validateStatus(request.getStatus());
        checkEmailUnique(request.getEmail(), id);
        checkPhoneUnique(request.getPhone(), id);

        String passwordHash = null;

        if (request.getPassword() != null
                && !request.getPassword().isBlank()) {
            passwordHash = passwordEncoder.encode(request.getPassword());
        }

        boolean updated = lambdaUpdate()
                .eq(UserEntity::getId, id)
                .set(UserEntity::getEmail, request.getEmail())
                .set(UserEntity::getPhone, request.getPhone())
                .set(
                        passwordHash != null,
                        UserEntity::getPasswordHash,
                        passwordHash
                )
                .set(UserEntity::getNickname, request.getNickname())
                .set(UserEntity::getAvatarUrl, request.getAvatarUrl())
                .set(UserEntity::getStatus, request.getStatus())
                .update();

        if (!updated) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        goalhubLogService.bizLog(
                MODULE_NAME,
                "UPDATE_APP_USER",
                null,
                null,
                "更新应用用户成功，userId=" + id
        );
    }
    /**
     * 删除应用用户。
     *
     * @param id 应用用户 ID
     */
    @Override
    public void delete(Long id) {
        checkUserExists(id);

        boolean removed = removeById(id);

        if (!removed) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        goalhubLogService.bizLog(
                MODULE_NAME,
                "DELETE_APP_USER",
                null,
                null,
                "删除应用用户成功，userId=" + id
        );
    }

    /**
     * 修改应用用户密码。
     *
     * @param id      应用用户 ID
     * @param request 密码更新参数
     */
    @Override
    public void updatePassword(Long id, UserAdminPasswordUpdateRequest request) {
        checkUserExists(id);

        boolean updated = lambdaUpdate()
                .eq(UserEntity::getId, id)
                .set(UserEntity::getPasswordHash, passwordEncoder.encode(request.getPassword()))
                .update();

        if (!updated) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        goalhubLogService.bizLog(
                MODULE_NAME,
                "UPDATE_APP_USER_PASSWORD",
                null,
                null,
                "修改应用用户密码成功，userId=" + id
        );
    }

    /**
     * 校验应用用户是否存在。
     *
     * @param id 应用用户 ID
     */
    private void checkUserExists(Long id) {
        UserEntity user = getById(id);

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
    }

    /**
     * 初始化分页参数。
     *
     * @param request 分页请求
     */
    private void initPage(UserAdminPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }

        if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 校验用户状态。
     *
     * @param status 用户状态
     */
    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验用户名唯一。
     *
     * @param username 用户名
     * @param excludeId 排除的用户 ID
     */
    private void checkUsernameUnique(String username, Long excludeId) {
        if (username == null || username.isBlank()) {
            throw new BusinessException(ResultCode.USERNAME_NOT_NULL);
        }

        Long count = lambdaQuery()
                .eq(UserEntity::getUsername, username.trim())
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .count();

        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
    }

    /**
     * 校验邮箱唯一。
     *
     * @param email 邮箱
     * @param excludeId 排除的用户 ID
     */
    private void checkEmailUnique(String email, Long excludeId) {
        if (email == null || email.isBlank()) {
            return;
        }

        Long count = lambdaQuery()
                .eq(UserEntity::getEmail, email.trim())
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .count();

        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验手机号唯一。
     *
     * @param phone 手机号
     * @param excludeId 排除的用户 ID
     */
    private void checkPhoneUnique(String phone, Long excludeId) {
        if (phone == null || phone.isBlank()) {
            return;
        }

        Long count = lambdaQuery()
                .eq(UserEntity::getPhone, phone.trim())
                .ne(excludeId != null, UserEntity::getId, excludeId)
                .count();

        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
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
