package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminUser;
import com.eugene.goalhub.admin.mapper.AdminUserMapper;
import com.eugene.goalhub.admin.service.AdminUserService;
import dto.AdminLoginRequest;
import dto.AdminPasswordUpdateRequest;
import dto.AdminUserCreateRequest;
import dto.AdminUserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser>
        implements AdminUserService {

    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Object login(AdminLoginRequest request) {
        AdminUser user = lambdaQuery()
                .eq(AdminUser::getUsername, request.getUsername())
                .one();

        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (Integer.valueOf(1).equals(user.getDeleted())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("管理员已禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

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

    @Override
    public Long create(AdminUserCreateRequest request) {
        boolean exists = lambdaQuery()
                .eq(AdminUser::getUsername, request.getUsername())
                .exists();

        if (exists) {
            throw new RuntimeException("管理员账号已存在");
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

    @Override
    public void update(Long id, AdminUserUpdateRequest request) {
        AdminUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("管理员不存在");
        }

        user.setNickname(request.getNickname());
        user.setStatus(request.getStatus());
        user.setIsSuperAdmin(request.getIsSuperAdmin());

        updateById(user);
    }

    @Override
    public void delete(Long id) {
        AdminUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("管理员不存在");
        }

        if (Integer.valueOf(1).equals(user.getIsSuperAdmin())) {
            throw new RuntimeException("超级管理员不能删除");
        }

        removeById(id);
    }

    @Override
    public void updatePassword(Long id, AdminPasswordUpdateRequest request) {
        AdminUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("管理员不存在");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        updateById(user);
    }
}