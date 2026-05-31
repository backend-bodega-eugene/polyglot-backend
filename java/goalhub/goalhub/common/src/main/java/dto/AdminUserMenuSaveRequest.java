package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理员菜单权限保存请求。
 */
@Schema(description = "管理员菜单权限保存请求")
@Data
public class AdminUserMenuSaveRequest {

    /**
     * 授权给管理员的菜单 ID 列表。
     */
    @Schema(description = "授权给管理员的菜单 ID 列表", example = "[1,2,3]")
    private List<Long> menuIds;
}
