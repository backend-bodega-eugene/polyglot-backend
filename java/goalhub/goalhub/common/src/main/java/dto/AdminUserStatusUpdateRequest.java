package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理员状态更新请求。
 */
@Schema(description = "管理员状态更新请求")
@Data
public class AdminUserStatusUpdateRequest {

    /**
     * 管理员状态：1 启用，0 禁用。
     */
    @Schema(description = "管理员状态：1 启用，0 禁用", example = "1")
    private Integer status;
}
