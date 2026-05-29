package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminUser;
import com.eugene.goalhub.admin.mapper.AdminUserMapper;
import com.eugene.goalhub.admin.service.AdminUserService;
import dto.*;
import exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import response.ResultCode;
import utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 后台管理员账号管理服务实现。
 */
@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser>
        implements AdminUserService {

    /**
     * 密码加密与校验组件。
     */
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 管理员登录。
     * <p>
     * 登录成功后更新最近登录时间，并签发后台管理 JWT。
     *
     * @param request 登录参数
     * @return token 和管理员基础信息
     */
    @Override
    public Object login(AdminLoginRequest request) {
        AdminUser user = lambdaQuery()
                .eq(AdminUser::getUsername, request.getUsername())
                .one();

        if (user == null) {
            // 用户不存在时返回统一错误，避免暴露账号是否存在。
            //throw new RuntimeException("用户名或密码错误");
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        if (Integer.valueOf(1).equals(user.getDeleted())) {
            // 已删除账号按登录失败处理。
            //throw new RuntimeException("用户名或密码错误");
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        if (user.getStatus() == 0) {
           // throw new RuntimeException("管理员已禁用");
            throw new BusinessException(ResultCode.USERNAME_PASSWORD_WRONG);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            //throw new RuntimeException("用户名或密码错误");
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

        return Map.of(
                "token", token,
                "id", user.getId(),
                "username", user.getUsername(),
                "nickname", user.getNickname(),
                "isSuperAdmin", user.getIsSuperAdmin()
        );
    }

    /**
     * 创建管理员账号。
     *
     * @param request 管理员创建参数
     * @return 新管理员 ID
     */
    @Override
    public Long create(AdminUserCreateRequest request) {
        boolean exists = lambdaQuery()
                .eq(AdminUser::getUsername, request.getUsername())
                .exists();

        if (exists) {
            //throw new RuntimeException("管理员账号已存在");
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        AdminUser user = new AdminUser();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setIsSuperAdmin(request.getIsSuperAdmin() == null ? 0 : request.getIsSuperAdmin());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        save(user);
        return user.getId();
    }

    /**
     * 更新管理员基础信息。
     *
     * @param id      管理员 ID
     * @param request 更新参数
     */
    @Override
    public void update(Long id, AdminUserUpdateRequest request) {
        AdminUser user = getById(id);
        if (user == null) {
           // throw new RuntimeException("管理员不存在");
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        user.setNickname(request.getNickname());
        user.setStatus(request.getStatus());
        user.setIsSuperAdmin(request.getIsSuperAdmin());

        updateById(user);
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
          //  throw new RuntimeException("管理员不存在");
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        if (Integer.valueOf(1).equals(user.getIsSuperAdmin())) {
           // throw new RuntimeException("超级管理员不能删除");
            throw new BusinessException(ResultCode.EUGENE_NOT_DELETE);
        }

        removeById(id);
    }

    /**
     * 修改管理员密码。
     *
     * @param id      管理员 ID
     * @param request 密码更新参数
     */
    @Override
    public void updatePassword(Long id, AdminPasswordUpdateRequest request) {
        AdminUser user = getById(id);
        if (user == null) {
           // throw new RuntimeException("管理员不存在");
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        updateById(user);
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
        Page<AdminUser> page = new Page<>(pageIndex, pageSize);

        Page<AdminUser> result = page(
                page,
                lambdaQuery()
                        .eq(AdminUser::getDeleted, 0)
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
     * @param id     管理员 ID
     * @param status 状态值
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        lambdaUpdate()
                .eq(AdminUser::getId, id)
                .set(AdminUser::getStatus, status)
                .update();
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
        response.setCreatedAt(adminUser.getCreatedAt());
        response.setUpdatedAt(adminUser.getUpdatedAt());
        return response;
    }
}
