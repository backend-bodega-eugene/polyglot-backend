package com.eugene.goalhub.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.admin.entity.AdminMenu;
import com.eugene.goalhub.admin.mapper.AdminMenuMapper;
import com.eugene.goalhub.admin.service.AdminMenuService;
import dto.AdminMenuCreateRequest;
import dto.AdminMenuTreeResponse;
import dto.AdminMenuUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminMenuServiceImpl extends ServiceImpl<AdminMenuMapper, AdminMenu>
        implements AdminMenuService {

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

            if (parentId == null || parentId == 0) {
                tree.add(menu);
                continue;
            }

            AdminMenuTreeResponse parent = menuMap.get(parentId);
            if (parent == null) {
                tree.add(menu);
            } else {
                parent.getChildren().add(menu);
            }
        }

        return tree;
    }

    @Override
    public Long create(AdminMenuCreateRequest request) {
        checkMenuType(request.getType());

        AdminMenu menu = new AdminMenu();
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        menu.setDeleted(0);

        save(menu);

        return menu.getId();
    }

    @Override
    public void update(Long id, AdminMenuUpdateRequest request) {
        AdminMenu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }

        checkMenuType(request.getType());

        Long parentId = request.getParentId() == null ? 0L : request.getParentId();
        if (Objects.equals(id, parentId)) {
            throw new RuntimeException("父级菜单不能是自己");
        }

        menu.setParentId(parentId);
        menu.setName(request.getName());
        menu.setType(request.getType());
        menu.setPath(request.getPath());
        menu.setIcon(request.getIcon());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setStatus(request.getStatus() == null ? 1 : request.getStatus());

        updateById(menu);
    }

    @Override
    public void delete(Long id) {
        AdminMenu menu = getById(id);
        if (menu == null) {
            throw new RuntimeException("菜单不存在");
        }

        boolean hasChildren = lambdaQuery()
                .eq(AdminMenu::getParentId, id)
                .exists();

        if (hasChildren) {
            throw new RuntimeException("该菜单存在子菜单，不能直接删除");
        }

        removeById(id);
    }

    private void checkMenuType(Integer type) {
        if (type == null) {
            throw new RuntimeException("菜单类型不能为空");
        }

        if (type != 1 && type != 2 && type != 3) {
            throw new RuntimeException("菜单类型错误");
        }
    }

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
}