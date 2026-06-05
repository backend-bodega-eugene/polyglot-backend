package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员状态更新请求。
 *
 * <p>用于后台启用或禁用管理员账号。</p>
 */
@Schema(description = "管理员状态更新请求")
@Data
public class AdminUserStatusUpdateRequest {

    /**
     * 管理员状态：1 启用，0 禁用。
     */
    @Schema(description = "管理员状态：1 启用，0 禁用", example = "1")
    @NotNull(message = "parameter.error")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer status;
}
