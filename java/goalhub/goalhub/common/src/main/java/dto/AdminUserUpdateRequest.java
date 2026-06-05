package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台管理员更新请求。
 *
 * <p>用于更新管理员昵称、启用状态和超级管理员标识。</p>
 */
@Schema(description = "后台管理员更新请求")
@Data
public class AdminUserUpdateRequest {

    /**
     * 管理员昵称。
     */
    @Schema(description = "管理员昵称", example = "管理员")
    private String nickname;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    @NotNull(message = "parameter.error")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer status;

    /**
     * 是否超级管理员：1 是，0 否。
     */
    @Schema(description = "是否超级管理员：1 是，0 否", example = "0")
    @NotNull(message = "parameter.error")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer isSuperAdmin;
}
