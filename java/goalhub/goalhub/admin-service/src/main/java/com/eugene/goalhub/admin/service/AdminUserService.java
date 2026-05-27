package com.eugene.goalhub.admin.service;

import dto.AdminLoginRequest;
import dto.AdminPasswordUpdateRequest;
import dto.AdminUserCreateRequest;
import dto.AdminUserUpdateRequest;

public interface AdminUserService {

    Object login(AdminLoginRequest request);

    Long create(AdminUserCreateRequest request);

    void update(Long id, AdminUserUpdateRequest request);

    void delete(Long id);

    void updatePassword(Long id, AdminPasswordUpdateRequest request);
}