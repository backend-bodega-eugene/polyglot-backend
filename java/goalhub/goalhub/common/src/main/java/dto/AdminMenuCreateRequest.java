package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台菜单创建请求。
 *
 * <p>用于创建后台菜单树节点，支持目录、菜单和按钮权限。</p>
 */
@Schema(description = "后台菜单创建请求")
@Data
public class AdminMenuCreateRequest {

    /**
     * 父菜单 ID，0 或空表示顶级菜单。
     */
    @Schema(description = "父菜单 ID，0 或空表示顶级菜单", example = "0")
    private Long parentId;

    /**
     * 菜单名称。
     */
    @Schema(description = "菜单名称", example = "用户管理")
    @NotBlank(message = "parameter.error")
    private String name;

    /**
     * 菜单类型：1 目录，2 菜单，3 按钮。
     */
    @Schema(description = "菜单类型：1 目录，2 菜单，3 按钮", example = "2")
    @NotNull(message = "menu.type.not.null")
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
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer status;
}
