package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminMenu;
import com.eugene.goalhub.admin.entity.AdminUser;
import com.eugene.goalhub.admin.entity.AdminUserMenu;
import com.eugene.goalhub.admin.mapper.AdminMenuMapper;
import com.eugene.goalhub.admin.mapper.AdminUserMapper;
import com.eugene.goalhub.admin.mapper.AdminUserMenuMapper;
import com.eugene.goalhub.admin.service.AdminUserMenuService;
import com.eugene.goalhub.admin.service.support.AdminOperationLogger;
import dto.AdminMenuTreeResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理员菜单权限服务实现。
 *
 * <p>负责管理员菜单授权关系维护，以及按授权关系组装后台菜单树。</p>
 */
@Service
public class AdminUserMenuServiceImpl
        extends ServiceImpl<AdminUserMenuMapper, AdminUserMenu>
        implements AdminUserMenuService {

    /**
     * 菜单 Mapper，用于查询菜单详情。
     */
    private final AdminMenuMapper adminMenuMapper;

    /**
     * 管理员 Mapper，用于查询管理员身份。
     */
    private final AdminUserMapper adminUserMapper;

    /**
     * 后台操作日志工具。
     */
    private final AdminOperationLogger adminOperationLogger;

    /**
     * 创建管理员菜单权限服务实现。
     *
     * @param adminMenuMapper      菜单 Mapper
     * @param adminUserMapper      管理员 Mapper
     * @param adminOperationLogger 后台操作日志工具
     */
    public AdminUserMenuServiceImpl(AdminMenuMapper adminMenuMapper,
                                    AdminUserMapper adminUserMapper,
                                    AdminOperationLogger adminOperationLogger) {
        this.adminMenuMapper = adminMenuMapper;
        this.adminUserMapper = adminUserMapper;
        this.adminOperationLogger = adminOperationLogger;
    }

    /**
     * 查询指定管理员拥有的菜单，并组装为树形结构。
     *
     * @param adminUserId 管理员 ID
     * @return 菜单树列表
     */
    @Override
    public List<AdminMenuTreeResponse> getUserMenus(Long adminUserId) {
        return getUserMenus(adminUserId, false);
    }

    /**
     * 查询指定管理员拥有的菜单，并组装为树形结构。
     *
     * @param adminUserId 管理员 ID
     * @param enabledOnly 是否只返回启用菜单
     * @return 菜单树列表
     */
    private List<AdminMenuTreeResponse> getUserMenus(Long adminUserId, boolean enabledOnly) {
        checkAdminUserExists(adminUserId);

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

        List<AdminMenu> menus = findMenusWithAncestors(menuIds, enabledOnly);

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
        checkAdminUserExists(adminUserId);

        Set<Long> normalizedMenuIds = normalizeMenuIds(menuIds);
        List<AdminMenu> menus = findMenus(normalizedMenuIds, false);
        if (menus.size() != normalizedMenuIds.size()) {
            throw new BusinessException(ResultCode.MENU_NOT_EXISTS);
        }

        Set<Long> menuIdsWithAncestors = collectMenuIdsWithAncestors(menus);

        lambdaUpdate()
                .eq(AdminUserMenu::getAdminUserId, adminUserId)
                .remove();

        if (menuIdsWithAncestors.isEmpty()) {
            adminOperationLogger.bizLog(
                    "后台管理员菜单权限",
                    "SAVE_ADMIN_USER_MENUS",
                    "保存管理员菜单权限成功，adminUserId=" + adminUserId + ", menuCount=0"
            );
            return;
        }

        List<AdminUserMenu> relations = menuIdsWithAncestors.stream()
                .map(menuId -> {
                    AdminUserMenu relation = new AdminUserMenu();
                    relation.setAdminUserId(adminUserId);
                    relation.setMenuId(menuId);
                    return relation;
                })
                .toList();

        saveBatch(relations);
        adminOperationLogger.bizLog(
                "后台管理员菜单权限",
                "SAVE_ADMIN_USER_MENUS",
                "保存管理员菜单权限成功，adminUserId=" + adminUserId + ", menuCount=" + relations.size()
        );
    }

    /**
     * 查询当前登录管理员可访问的菜单。
     * <p>
     * 超级管理员可访问全部启用菜单，其他管理员按授权关系查询。
     *
     * @param adminUserId 管理员 ID
     * @param username    管理员用户名
     * @return 菜单树列表
     */
    @Override
    public List<AdminMenuTreeResponse> getCurrentLoginMenus(Long adminUserId, String username) {
        AdminUser adminUser = adminUserMapper.selectById(adminUserId);
        if (adminUser == null) {
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }

        if (Integer.valueOf(1).equals(adminUser.getIsSuperAdmin())) {
            List<AdminMenu> menus = adminMenuMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminMenu>()
                            .eq(AdminMenu::getStatus, 1)
                            .orderByAsc(AdminMenu::getSortOrder)
            );

            return buildTree(menus);
        }

        return getUserMenus(adminUserId, true);
    }

    /**
     * 将菜单列表按 parentId 组装为树形结构。
     *
     * <p>仅返回 parentId 为 0 的根节点，子节点挂载到各自父节点的 children 中。</p>
     *
     * @param menus 菜单实体列表
     * @return 菜单树列表
     */
    private List<AdminMenuTreeResponse> buildTree(List<AdminMenu> menus) {
        List<AdminMenuTreeResponse> responses = menus.stream()
                .map(this::toResponse)
                .toList();

        Map<Long, List<AdminMenuTreeResponse>> childrenMap = responses.stream()
                .collect(Collectors.groupingBy(item -> item.getParentId() == null
                        ? 0L
                        : item.getParentId()));

        responses.forEach(item -> item.setChildren(
                childrenMap.getOrDefault(item.getId(), List.of())
        ));

        return responses.stream()
                .filter(item -> item.getParentId() == null || item.getParentId() == 0)
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

    /**
     * 校验管理员存在。
     *
     * @param adminUserId 管理员 ID
     */
    private void checkAdminUserExists(Long adminUserId) {
        if (adminUserId == null || adminUserMapper.selectById(adminUserId) == null) {
            throw new BusinessException(ResultCode.USERNAME__NOT_EXISTS);
        }
    }

    /**
     * 规范化菜单 ID，过滤 null 和逻辑根 0。
     *
     * @param menuIds 菜单 ID 列表
     * @return 规范化后的菜单 ID 集合
     */
    private Set<Long> normalizeMenuIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return Set.of();
        }

        Set<Long> normalizedMenuIds = menuIds.stream()
                .filter(Objects::nonNull)
                .filter(menuId -> menuId != 0)
                .collect(Collectors.toSet());

        boolean hasInvalidMenuId = normalizedMenuIds.stream()
                .anyMatch(menuId -> menuId < 0);
        if (hasInvalidMenuId) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return normalizedMenuIds;
    }

    /**
     * 根据菜单 ID 查询菜单。
     *
     * @param menuIds     菜单 ID 集合
     * @param enabledOnly 是否只查询启用菜单
     * @return 菜单列表
     */
    private List<AdminMenu> findMenus(Set<Long> menuIds, boolean enabledOnly) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }

        return adminMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AdminMenu>()
                        .in(AdminMenu::getId, menuIds)
                        .eq(enabledOnly, AdminMenu::getStatus, 1)
                        .orderByAsc(AdminMenu::getSortOrder)
                        .orderByAsc(AdminMenu::getId)
        );
    }

    /**
     * 查询菜单并补齐父级菜单。
     *
     * @param menuIds     菜单 ID 列表
     * @param enabledOnly 是否只查询启用菜单
     * @return 菜单及父级菜单列表
     */
    private List<AdminMenu> findMenusWithAncestors(List<Long> menuIds, boolean enabledOnly) {
        Set<Long> normalizedMenuIds = normalizeMenuIds(menuIds);
        List<AdminMenu> menus = findMenus(normalizedMenuIds, enabledOnly);
        Set<Long> menuIdsWithAncestors = collectMenuIdsWithAncestors(menus);
        return findMenus(menuIdsWithAncestors, enabledOnly);
    }

    /**
     * 收集菜单及所有父级菜单 ID。
     *
     * @param menus 菜单列表
     * @return 菜单及父级菜单 ID 集合
     */
    private Set<Long> collectMenuIdsWithAncestors(List<AdminMenu> menus) {
        Set<Long> menuIdsWithAncestors = new HashSet<>();
        for (AdminMenu menu : menus) {
            collectMenuIdWithAncestors(menu, menuIdsWithAncestors);
        }
        return menuIdsWithAncestors;
    }

    /**
     * 收集单个菜单及其父级菜单 ID。
     *
     * @param menu                 菜单
     * @param menuIdsWithAncestors 菜单及父级菜单 ID 集合
     */
    private void collectMenuIdWithAncestors(AdminMenu menu, Set<Long> menuIdsWithAncestors) {
        if (menu == null || menu.getId() == null || !menuIdsWithAncestors.add(menu.getId())) {
            return;
        }

        Long parentId = menu.getParentId();
        Set<Long> visitedParentIds = new HashSet<>();
        while (parentId != null && parentId != 0) {
            if (!visitedParentIds.add(parentId)) {
                throw new BusinessException(ResultCode.FATHER_NOT_CHILD_CODE);
            }

            AdminMenu parentMenu = adminMenuMapper.selectById(parentId);
            if (parentMenu == null) {
                throw new BusinessException(ResultCode.MENU_NOT_EXISTS);
            }

            if (!menuIdsWithAncestors.add(parentMenu.getId())) {
                return;
            }
            parentId = parentMenu.getParentId();
        }
    }
}
