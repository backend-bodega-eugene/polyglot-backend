package dto;

import lombok.Data;

/**
 * 后台菜单更新请求。
 */
@Data
public class AdminMenuUpdateRequest {

    /**
     * 父菜单 ID，0 或空表示顶级菜单。
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
}
