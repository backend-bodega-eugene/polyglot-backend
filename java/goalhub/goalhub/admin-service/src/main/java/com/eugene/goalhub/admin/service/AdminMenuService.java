package com.eugene.goalhub.admin.service;

import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;

import java.util.List;

public interface AdminMenuService {

    List<AdminMenuTreeResponse> tree();

    Long create(AdminMenuCreateRequest request);

    void update(Long id, AdminMenuUpdateRequest request);

    void delete(Long id);
}