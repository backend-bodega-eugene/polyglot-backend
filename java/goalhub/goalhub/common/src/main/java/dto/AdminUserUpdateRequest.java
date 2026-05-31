package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台管理员更新请求。
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
    private Integer status;

    /**
     * 是否超级管理员：1 是，0 否。
     */
    @Schema(description = "是否超级管理员：1 是，0 否", example = "0")
    private Integer isSuperAdmin;
}
