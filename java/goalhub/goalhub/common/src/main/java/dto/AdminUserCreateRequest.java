package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 后台管理员创建请求。
 *
 * <p>用于创建后台管理员账号。</p>
 */
@Schema(description = "后台管理员创建请求")
@Data
public class AdminUserCreateRequest {

    /**
     * 管理员账号。
     */
    @Schema(description = "管理员账号", example = "admin")
    @NotBlank(message = "username.cannot.be.empty")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码", example = "P@ssw0rdDemo")
    @NotBlank(message = "password.cannot.be.empty")
    private String password;

    /**
     * 管理员昵称。
     */
    @Schema(description = "管理员昵称", example = "管理员")
    private String nickname;

    /**
     * 是否超级管理员：1 是，0 否。
     */
    @Schema(description = "是否超级管理员：1 是，0 否", example = "0")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer isSuperAdmin;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer status;
}
