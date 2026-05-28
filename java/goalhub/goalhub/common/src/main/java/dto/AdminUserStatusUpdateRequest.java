package dto;

import lombok.Data;

/**
 * 管理员状态更新请求。
 */
@Data
public class AdminUserStatusUpdateRequest {

    /**
     * 管理员状态：1 启用，0 禁用。
     */
    private Integer status;
}
