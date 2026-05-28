package dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台菜单树节点响应。
 */
@Data
public class AdminMenuTreeResponse {

    /**
     * 菜单 ID。
     */
    private Long id;

    /**
     * 父菜单 ID。
     */
    private Long parentId;

    /**
     * 菜单名称。
     */
    private String name;

    /**
     * 菜单类型：1 目录，2 菜单，3 按钮。
     */
    private Integer type;

    /**
     * 前端路由路径或按钮权限标识。
     */
    private String path;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 排序值，数值越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 状态：1 启用，0 禁用。
     */
    private Integer status;

    /**
     * 子菜单节点列表。
     */
    private List<AdminMenuTreeResponse> children = new ArrayList<>();
}
