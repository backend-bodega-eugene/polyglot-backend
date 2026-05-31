package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录请求。
 */
@Schema(description = "用户登录请求")
@Data
public class LoginRequest {

    /**
     * 登录账号，可为用户名、邮箱或手机号。
     */
    @Schema(description = "登录账号，可为用户名、邮箱或手机号", example = "zhangsan")
    private String account;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码", example = "123456")
    private String password;
}
