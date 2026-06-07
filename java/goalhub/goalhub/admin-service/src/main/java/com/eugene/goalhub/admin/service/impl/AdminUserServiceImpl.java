package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminUser;
import com.eugene.goalhub.admin.mapper.AdminUserMapper;
import com.eugene.goalhub.admin.service.AdminUserService;
import com.eugene.goalhub.admin.service.support.AdminOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import response.ResultCode;
import utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 后台管理员账号管理服务实现。
 *
 * <p>负责后台管理员登录、账号维护、密码处理和启用状态管理。</p>
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser>
        implements AdminUserService {

    /**
     * 密码加密与校验组件。
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 后台操作日志工具。
     */
    private final AdminOperationLogger adminOperationLogger;

    /**
     * 创建后台管理员账号管理服务实现。
     *
     * @param passwordEncoder      密码加密与校验组件
     * @param adminOperationLogger 后台操作日志工具
     */
    public AdminUserServiceImpl(
            PasswordEncoder passwordEncoder,
            AdminOperationLogger adminOperationLogger
    ) {
        this.passwordEncoder = passwordEncoder;
        this.adminOperationLogger = adminOperationLogger;
    }

    /**
     * 管理员登录。
     * <p>
     * 登录成功后更新最近登录时间，并签发后台管理 JWT。
     * 登录失败时统一返回账号或密码错误，避免暴露账号状态细节。
     *
     * @param request 登录参数
     * @return token 和管理员基础信息
     */
    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        requireRequest(request);
        requireNotBlank(request.getUsername());
        requireNotBlank(request.getPassword());

        AdminUser user = lambdaQuery()
                .eq(AdminUser::getUsername, request.getUsername())
                .one();

        if (user == null) {
            // 用户不存在时返回统一错误，避免暴露账号是否存在。
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        if (Integer.valueOf(1).equals(user.getDeleted())) {
            // 已删除账号按登录失败处理。
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        // 登录成功后记录最近登录时间。
        user.setLastLoginAt(LocalDateTime.now());
        updateById(user);

        String role = Integer.valueOf(1).equals(user.getIsSuperAdmin())
                ? "SUPER_ADMIN"
                : "ADMIN";

        String token = JwtUtil.adminGenerateToken(
                user.getId(),
                user.getUsername(),
                role
        );

        AdminLoginResponse response = new AdminLoginResponse();
        response.setToken(token);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setIsSuperAdmin(user.getIsSuperAdmin());
        adminOperationLogger.bizLog(
                "后台管理员账号",
                "ADMIN_LOGIN",
                user.getId(),
                user.getUsername(),
                "管理员登录成功"
        );
        return response;
    }

    /**
     * 创建管理员账号。
     *
     * @param request             管理员创建参数
     * @param operatorAdminUserId 当前操作管理员 ID
     * @return 新管理员 ID
     */
    @Override
    public Long create(AdminUserCreateRequest request, Long operatorAdminUserId) {
        requireRequest(request);
        requireNotBlank(request.getUsername());
        requireNotBlank(request.getPassword());
        requireValidBinaryValue(request.getIsSuperAdmin());
        requireValidBinaryValue(request.getStatus());
        if (Integer.valueOf(1).equals(request.getIsSuperAdmin())) {
            requireSuperAdmin(operatorAdminUserId);
        }

        boolean exists = lambdaQuery()
                .eq(AdminUser::getUsername, request.getUsername())
                .exists();

        if (exists) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        AdminUser user = new AdminUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setIsSuperAdmin(request.getIsSuperAdmin() == null ? 0 : request.getIsSuperAdmin());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        save(user);
        adminOperationLogger.bizLog(
                "后台管理员账号",
                "CREATE_ADMIN_USER",
                "创建管理员账号成功，adminUserId=" + user.getId()
        );
        return user.getId();
    }

    /**
     * 更新管理员基础信息。
     *
     * @param id                  管理员 ID
     * @param operatorAdminUserId 当前操作管理员 ID
     * @param request             更新参数
     */
    @Override
    public void update(Long id, Long operatorAdminUserId, AdminUserUpdateRequest request) {
        requireRequest(request);
        requireRequiredBinaryValue(request.getStatus());
        requireRequiredBinaryValue(request.getIsSuperAdmin());

        AdminUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        if (!Objects.equals(user.getIsSuperAdmin(), request.getIsSuperAdmin())) {
            requireSuperAdmin(operatorAdminUserId);
        }

        user.setNickname(request.getNickname());
        user.setStatus(request.getStatus());
        user.setIsSuperAdmin(request.getIsSuperAdmin());

        updateById(user);
        adminOperationLogger.bizLog(
                "后台管理员账号",
                "UPDATE_ADMIN_USER",
                "更新管理员账号成功，adminUserId=" + id
        );
    }

    /**
     * 删除管理员账号。
     * <p>
     * 超级管理员账号不允许删除。
     *
     * @param id 管理员 ID
     */
    @Override
    public void delete(Long id) {
        AdminUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        if (Integer.valueOf(1).equals(user.getIsSuperAdmin())) {
            throw new BusinessException(ResultCode.EUGENE_NOT_DELETE);
        }

        removeById(id);
        adminOperationLogger.bizLog(
                "后台管理员账号",
                "DELETE_ADMIN_USER",
                "删除管理员账号成功，adminUserId=" + id
        );
    }

    /**
     * 修改管理员密码。
     *
     * @param id      管理员 ID
     * @param request 密码更新参数
     */
    @Override
    public void updatePassword(Long id, AdminPasswordUpdateRequest request) {
        requireRequest(request);
        requireNotBlank(request.getPassword());

        AdminUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        updateById(user);
        adminOperationLogger.bizLog(
                "后台管理员账号",
                "UPDATE_ADMIN_PASSWORD",
                "修改管理员密码成功，adminUserId=" + id
        );
    }

    /**
     * 分页查询管理员账号。
     *
     * @param pageIndex 页码
     * @param pageSize  每页数量
     * @param username  用户名筛选条件
     * @return 管理员分页数据
     */
    @Override
    public PageResponse<AdminUserPageResponse> page(Integer pageIndex, Integer pageSize, String username) {
        if (pageIndex == null || pageIndex < 1 || pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Page<AdminUser> page = new Page<>(pageIndex, pageSize);

        Page<AdminUser> result = page(
                page,
                lambdaQuery()
                        .like(username != null && !username.isBlank(), AdminUser::getUsername, username)
                        .orderByDesc(AdminUser::getCreatedAt)
                        .getWrapper()
        );

        List<AdminUserPageResponse> records = result.getRecords()
                .stream()
                .map(this::toPageResponse)
                .toList();

        PageResponse<AdminUserPageResponse> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageIndex(pageIndex);
        response.setPageSize(pageSize);
        response.setRecords(records);

        return response;
    }

    /**
     * 更新管理员启用状态。
     *
     * <p>超级管理员账号不允许通过该接口禁用或启用。</p>
     *
     * @param id     管理员 ID
     * @param status 状态值
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        requireRequiredBinaryValue(status);

        AdminUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        if (Integer.valueOf(1).equals(user.getIsSuperAdmin())) {
            throw new BusinessException(ResultCode.SUPER_ADMIN_STATUS_NOT_ALLOW_UPDATE_CODE);
        }

        user.setStatus(status);
        updateById(user);
        adminOperationLogger.bizLog(
                "后台管理员账号",
                "UPDATE_ADMIN_STATUS",
                "更新管理员状态成功，adminUserId=" + id + ", status=" + status
        );
    }

    /**
     * 将管理员实体转换为分页响应对象。
     *
     * @param adminUser 管理员实体
     * @return 管理员分页响应对象
     */
    private AdminUserPageResponse toPageResponse(AdminUser adminUser) {
        AdminUserPageResponse response = new AdminUserPageResponse();
        response.setId(adminUser.getId());
        response.setUsername(adminUser.getUsername());
        response.setNickname(adminUser.getNickname());
        response.setStatus(adminUser.getStatus());
        response.setIsSuperAdmin(adminUser.getIsSuperAdmin());
        response.setCreatedAt(adminUser.getCreatedAt());
        response.setUpdatedAt(adminUser.getUpdatedAt());
        return response;
    }

    /**
     * 校验请求对象不能为空。
     *
     * @param request 请求对象
     */
    private void requireRequest(Object request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验文本不能为空。
     *
     * @param value 文本
     */
    private void requireNotBlank(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验二值字段只能为 0 或 1，null 表示使用默认值时允许。
     *
     * @param value 二值字段
     */
    private void requireValidBinaryValue(Integer value) {
        if (value != null && value != 0 && value != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验必填二值字段只能为 0 或 1。
     *
     * @param value 二值字段
     */
    private void requireRequiredBinaryValue(Integer value) {
        if (value == null || value != 0 && value != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验当前操作人必须是超级管理员。
     *
     * @param operatorAdminUserId 当前操作管理员 ID
     */
    private void requireSuperAdmin(Long operatorAdminUserId) {
        if (operatorAdminUserId == null) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        AdminUser operator = getById(operatorAdminUserId);
        if (operator == null || !Integer.valueOf(1).equals(operator.getIsSuperAdmin())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
