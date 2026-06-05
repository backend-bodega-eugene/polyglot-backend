package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台菜单树节点响应。
 *
 * <p>返回后台菜单树节点及其子节点列表。</p>
 */
@Schema(description = "后台菜单树节点响应")
@Data
public class AdminMenuTreeResponse {

    /**
     * 菜单 ID。
     */
    @Schema(description = "菜单 ID", example = "1")
    private Long id;

    /**
     * 父菜单 ID。
     */
    @Schema(description = "父菜单 ID", example = "0")
    private Long parentId;

    /**
     * 菜单名称。
     */
    @Schema(description = "菜单名称", example = "用户管理")
    private String name;

    /**
     * 菜单类型：1 目录，2 菜单，3 按钮。
     */
    @Schema(description = "菜单类型：1 目录，2 菜单，3 按钮", example = "2")
    private Integer type;

    /**
     * 前端路由路径或按钮权限标识。
     */
    @Schema(description = "前端路由路径或按钮权限标识", example = "/admin/users")
    private String path;

    /**
     * 菜单图标。
     */
    @Schema(description = "菜单图标", example = "User")
    private String icon;

    /**
     * 排序值，数值越小越靠前。
     */
    @Schema(description = "排序值，数值越小越靠前", example = "10")
    private Integer sortOrder;

    /**
     * 状态：1 启用，0 禁用。
     */
    @Schema(description = "状态：1 启用，0 禁用", example = "1")
    private Integer status;

    /**
     * 子菜单节点列表。
     */
    @Schema(description = "子菜单节点列表")
    private List<AdminMenuTreeResponse> children = new ArrayList<>();
}
