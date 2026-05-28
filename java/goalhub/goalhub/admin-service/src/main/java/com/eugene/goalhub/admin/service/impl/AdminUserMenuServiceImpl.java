package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminMenu;
import com.eugene.goalhub.admin.entity.AdminUserMenu;
import com.eugene.goalhub.admin.mapper.AdminMenuMapper;
import com.eugene.goalhub.admin.mapper.AdminUserMenuMapper;
import com.eugene.goalhub.admin.service.AdminUserMenuService;
import dto.AdminMenuTreeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 管理员菜单权限服务实现。
 */
@Service
public class AdminUserMenuServiceImpl
        extends ServiceImpl<AdminUserMenuMapper, AdminUserMenu>
        implements AdminUserMenuService {

    /**
     * 菜单 Mapper，用于查询菜单详情。
     */
    private final AdminMenuMapper adminMenuMapper;

    public AdminUserMenuServiceImpl(AdminMenuMapper adminMenuMapper) {
        this.adminMenuMapper = adminMenuMapper;
    }

    /**
     * 查询指定管理员拥有的菜单，并组装为树形结构。
     *
     * @param adminUserId 管理员 ID
     * @return 菜单树列表
     */
    @Override
    public List<AdminMenuTreeResponse> getUserMenus(Long adminUserId) {
        List<Long> menuIds = lambdaQuery()
                .eq(AdminUserMenu::getAdminUserId, adminUserId)
                .list()
                .stream()
                .map(AdminUserMenu::getMenuId)
                .distinct()
                .toList();

        if (menuIds.isEmpty()) {
            return List.of();
        }

        // 只返回未删除的菜单，避免已删除权限继续出现在前端。
        List<AdminMenu> menus = adminMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminMenu>()
                        .in(AdminMenu::getId, menuIds)
                        .eq(AdminMenu::getDeleted, 0)
                        .orderByAsc(AdminMenu::getSortOrder)
        );

        return buildTree(menus);
    }

    /**
     * 保存管理员菜单权限。
     * <p>
     * 采用先删除后插入的方式，保证权限关系和提交的菜单 ID 列表一致。
     *
     * @param adminUserId 管理员 ID
     * @param menuIds     菜单 ID 列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserMenus(Long adminUserId, List<Long> menuIds) {
        lambdaUpdate()
                .eq(AdminUserMenu::getAdminUserId, adminUserId)
                .remove();

        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }

        List<AdminUserMenu> relations = menuIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(menuId -> {
                    AdminUserMenu relation = new AdminUserMenu();
                    relation.setAdminUserId(adminUserId);
                    relation.setMenuId(menuId);
                    return relation;
                })
                .toList();

        saveBatch(relations);
    }

    /**
     * 查询当前登录管理员可访问的菜单。
     * <p>
     * 默认超级管理员 eugene 可访问全部启用菜单，其他管理员按授权关系查询。
     *
     * @param adminUserId 管理员 ID
     * @param username    管理员用户名
     * @return 菜单树列表
     */
    @Override
    public List<AdminMenuTreeResponse> getCurrentLoginMenus(Long adminUserId, String username) {
        if ("eugene".equals(username)) {
            List<AdminMenu> menus = adminMenuMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminMenu>()
                            .eq(AdminMenu::getDeleted, 0)
                            .eq(AdminMenu::getStatus, 1)
                            .orderByAsc(AdminMenu::getSortOrder)
            );

            return buildTree(menus);
        }

        return getUserMenus(adminUserId);
    }

    /**
     * 将菜单列表按 parentId 组装为树形结构。
     *
     * @param menus 菜单实体列表
     * @return 菜单树列表
     */
    private List<AdminMenuTreeResponse> buildTree(List<AdminMenu> menus) {
        List<AdminMenuTreeResponse> responses = menus.stream()
                .map(this::toResponse)
                .toList();

        Map<Long, List<AdminMenuTreeResponse>> childrenMap = responses.stream()
                .collect(Collectors.groupingBy(AdminMenuTreeResponse::getParentId));

        responses.forEach(item -> item.setChildren(
                childrenMap.getOrDefault(item.getId(), List.of())
        ));

        return responses.stream()
                .filter(item -> item.getParentId() == 0)
                .sorted(Comparator.comparing(AdminMenuTreeResponse::getSortOrder))
                .toList();
    }

    /**
     * 将菜单实体转换为菜单树响应节点。
     *
     * @param menu 菜单实体
     * @return 菜单树响应节点
     */
    private AdminMenuTreeResponse toResponse(AdminMenu menu) {
        AdminMenuTreeResponse response = new AdminMenuTreeResponse();
        response.setId(menu.getId());
        response.setParentId(menu.getParentId());
        response.setName(menu.getName());
        response.setType(menu.getType());
        response.setPath(menu.getPath());
        response.setIcon(menu.getIcon());
        response.setSortOrder(menu.getSortOrder());
        response.setStatus(menu.getStatus());
        return response;
    }
}
