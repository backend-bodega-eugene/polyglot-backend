package com.eugene.goalhub.admin.service;

import dto.AdminMenuTreeResponse;

import java.util.List;

/**
 * 管理员菜单权限服务。
 */
public interface AdminUserMenuService {

    /**
     * 查询指定管理员拥有的菜单权限。
     *
     * @param adminUserId 管理员 ID
     * @return 菜单树列表
     */
    List<AdminMenuTreeResponse> getUserMenus(Long adminUserId);

    /**
     * 保存指定管理员的菜单权限。
     *
     * @param adminUserId 管理员 ID
     * @param menuIds     菜单 ID 列表
     */
    void saveUserMenus(Long adminUserId, List<Long> menuIds);

    /**
     * 查询当前登录管理员可访问的菜单。
     *
     * @param adminUserId 管理员 ID
     * @param username    管理员用户名
     * @return 菜单树列表
     */
    List<AdminMenuTreeResponse> getCurrentLoginMenus(Long adminUserId, String username);
}
