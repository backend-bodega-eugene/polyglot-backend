package com.eugene.goalhub.admin.service;

import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;

import java.util.List;

/**
 * 后台菜单管理服务。
 */
public interface AdminMenuService {

    /**
     * 查询所有菜单并组装为树形结构。
     *
     * @return 菜单树列表
     */
    List<AdminMenuTreeResponse> tree();

    /**
     * 创建菜单。
     *
     * @param request 菜单创建参数
     * @return 新菜单 ID
     */
    Long create(AdminMenuCreateRequest request);

    /**
     * 更新菜单。
     *
     * @param id      菜单 ID
     * @param request 菜单更新参数
     */
    void update(Long id, AdminMenuUpdateRequest request);

    /**
     * 删除菜单。
     *
     * @param id 菜单 ID
     */
    void delete(Long id);
}
