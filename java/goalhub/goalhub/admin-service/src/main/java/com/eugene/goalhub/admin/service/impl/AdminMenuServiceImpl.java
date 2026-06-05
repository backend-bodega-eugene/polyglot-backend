package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminMenu;
import com.eugene.goalhub.admin.mapper.AdminMenuMapper;
import com.eugene.goalhub.admin.service.AdminMenuService;
import com.eugene.goalhub.admin.service.support.AdminOperationLogger;
import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台菜单管理服务实现。
 *
 * <p>负责后台菜单的树形查询、创建、更新、删除，以及菜单层级关系合法性校验。</p>
 */
@Service
public class AdminMenuServiceImpl extends ServiceImpl<AdminMenuMapper, AdminMenu>
        implements AdminMenuService {

    /**
     * 后台操作日志工具。
     */
    private final AdminOperationLogger adminOperationLogger;

    /**
     * 创建后台菜单管理服务实现。
     *
     * @param adminOperationLogger 后台操作日志工具
     */
    public AdminMenuServiceImpl(AdminOperationLogger adminOperationLogger) {
        this.adminOperationLogger = adminOperationLogger;
    }

    /**
     * 查询菜单列表，并按 parentId 组装为前端需要的树形结构。
     *
     * <p>父节点不存在的菜单会降级为根节点返回，避免异常数据导致菜单不可见。</p>
     *
     * @return 菜单树列表
     */
    @Override
    public List<AdminMenuTreeResponse> tree() {
        List<AdminMenu> menus = lambdaQuery()
                .orderByAsc(AdminMenu::getSortOrder)
                .orderByAsc(AdminMenu::getId)
                .list();

        List<AdminMenuTreeResponse> responses = menus.stream()
                .map(this::toTreeResponse)
                .collect(Collectors.toList());

        Map<Long, AdminMenuTreeResponse> menuMap = responses.stream()
                .collect(Collectors.toMap(AdminMenuTreeResponse::getId, item -> item));

        List<AdminMenuTreeResponse> tree = new ArrayList<>();

        for (AdminMenuTreeResponse menu : responses) {
            Long parentId = menu.getParentId();

            // 顶级菜单没有父节点，直接放入根节点列表。
            if (parentId == null || parentId == 0) {
                tree.add(menu);
                continue;
            }

            AdminMenuTreeResponse parent = menuMap.get(parentId);
            if (parent == null) {
                // 父节点不存在时降级为根节点，避免菜单丢失。
                tree.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }

        return tree;
    }

    /**
     * 创建菜单，缺省字段会补充默认值。
     *
     * @param request 菜单创建参数
     * @return 新菜单 ID
     */
    @Override
    public Long create(AdminMenuCreateRequest request) {
        requireRequest(request);
        checkMenuType(request.getType());
        requireValidBinaryValue(request.getStatus());

        Long parentId = normalizeParentId(request.getParentId());
        checkParentMenuExists(parentId);

        AdminMenu menu = new AdminMenu();
        menu.setParentId(parentId);
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        menu.setDeleted(0);

        save(menu);
        adminOperationLogger.bizLog(
                "后台菜单管理",
                "CREATE_MENU",
                "创建后台菜单成功，menuId=" + menu.getId()
        );

        return menu.getId();
    }

    /**
     * 更新菜单信息，并禁止把自身设置为父菜单。
     *
     * @param id      菜单 ID
     * @param request 菜单更新参数
     */
    @Override
    public void update(Long id, AdminMenuUpdateRequest request) {
        requireRequest(request);

        AdminMenu menu = getById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.MENU_NOT_EXISTS);
        }

        checkMenuType(request.getType());
        requireValidBinaryValue(request.getStatus());

        Long parentId = normalizeParentId(request.getParentId());
        if (Objects.equals(id, parentId)) {
            throw new BusinessException(ResultCode.FATHER_NOT_OWN);
        }

        checkParentMenuExists(parentId);

        if (isDescendantMenu(parentId, id)) {
            throw new BusinessException(ResultCode.FATHER_NOT_CHILD_CODE);
        }

        menu.setParentId(parentId);
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        updateById(menu);
        adminOperationLogger.bizLog(
                "后台菜单管理",
                "UPDATE_MENU",
                "更新后台菜单成功，menuId=" + id
        );
    }

    /**
     * 删除菜单。
     * <p>
     * 存在子菜单时不允许直接删除，避免产生孤儿菜单。
     *
     * @param id 菜单 ID
     */
    @Override
    public void delete(Long id) {
        AdminMenu menu = getById(id);
        if (menu == null) {
            throw new BusinessException(ResultCode.MENU_NOT_EXISTS);
        }

        boolean hasChildren = lambdaQuery()
                .eq(AdminMenu::getParentId, id)
                .exists();

        if (hasChildren) {
            throw new BusinessException(ResultCode.MENU_HAVE_CHILDREN);
        }

        removeById(id);
        adminOperationLogger.bizLog(
                "后台菜单管理",
                "DELETE_MENU",
                "删除后台菜单成功，menuId=" + id
        );
    }

    /**
     * 校验菜单类型。
     * <p>
     * 当前约定：1 目录，2 菜单，3 按钮。
     *
     * @param type 菜单类型
     */
    private void checkMenuType(Integer type) {
        if (type == null) {
            throw new BusinessException(ResultCode.MENU_TYPE_NOT_NULL);
        }

        if (type != 1 && type != 2 && type != 3) {
            throw new BusinessException(ResultCode.MENU_WRONG_TYPE);
        }
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
     * 规范化父菜单 ID。
     *
     * @param parentId 父菜单 ID
     * @return 规范化后的父菜单 ID
     */
    private Long normalizeParentId(Long parentId) {
        Long normalizedParentId = parentId == null ? 0L : parentId;
        if (normalizedParentId < 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
        return normalizedParentId;
    }

    /**
     * 校验父菜单存在，0 表示逻辑根节点。
     *
     * @param parentId 父菜单 ID
     */
    private void checkParentMenuExists(Long parentId) {
        if (parentId == null || parentId == 0) {
            return;
        }

        if (getById(parentId) == null) {
            throw new BusinessException(ResultCode.MENU_NOT_EXISTS);
        }
    }

    /**
     * 将菜单实体转换为树节点响应对象。
     *
     * @param menu 菜单实体
     * @return 菜单树节点
     */
    private AdminMenuTreeResponse toTreeResponse(AdminMenu menu) {
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
     * 判断目标父菜单是否为当前菜单的子孙节点。
     *
     * <p>用于防止更新菜单时形成循环引用。</p>
     *
     * @param targetParentId 目标父菜单 ID
     * @param currentMenuId  当前菜单 ID
     * @return 是子孙节点时返回 true
     */
    private boolean isDescendantMenu(Long targetParentId, Long currentMenuId) {
        if (targetParentId == null || targetParentId == 0) {
            return false;
        }

        Set<Long> visitedMenuIds = new HashSet<>();
        Long cursorMenuId = targetParentId;

        while (cursorMenuId != null && cursorMenuId != 0) {
            if (Objects.equals(cursorMenuId, currentMenuId)) {
                return true;
            }

            if (!visitedMenuIds.add(cursorMenuId)) {
                return false;
            }

            AdminMenu cursorMenu = getById(cursorMenuId);
            if (cursorMenu == null) {
                return false;
            }

            cursorMenuId = cursorMenu.getParentId();
        }

        return false;
    }
}
