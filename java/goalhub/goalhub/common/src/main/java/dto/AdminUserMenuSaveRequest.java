package dto;

import lombok.Data;

import java.util.List;

/**
 * 管理员菜单权限保存请求。
 */
@Data
public class AdminUserMenuSaveRequest {

    /**
     * 授权给管理员的菜单 ID 列表。
     */
    private List<Long> menuIds;
}
