package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台管理员登录请求。
 */
@Schema(description = "后台管理员登录请求")
@Data
public class AdminLoginRequest {

    /**
     * 管理员账号。
     */
    @Schema(description = "管理员账号", example = "admin")
    @NotBlank(message = "username.cannot.be.empty")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码", example = "123456")
    @NotBlank(message = "password.cannot.be.empty")
    private String password;
}
