package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 管理员菜单权限保存请求。
 *
 * <p>用于保存指定管理员可访问的菜单 ID 列表，空列表表示清空权限。</p>
 */
@Schema(description = "管理员菜单权限保存请求")
@Data
public class AdminUserMenuSaveRequest {

    /**
     * 授权给管理员的菜单 ID 列表。
     */
    @Schema(description = "授权给管理员的菜单 ID 列表", example = "[1,2,3]")
    @NotNull(message = "parameter.error")
    private List<Long> menuIds;
}
