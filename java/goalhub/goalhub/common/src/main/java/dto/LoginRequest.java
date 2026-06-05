package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 100, message = "登录账号长度不能超过100")
    @Schema(description = "登录账号，可为用户名、邮箱或手机号", example = "zhangsan")
    private String account;

    /**
     * 登录密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在6到64之间")
    @Schema(description = "登录密码", example = "P@ssw0rdDemo")
    private String password;

    /**
     * 验证码 key。
     */
    @NotBlank(message = "验证码key不能为空")
    @Size(max = 64, message = "验证码key长度不能超过64")
    @Schema(description = "验证码 key", example = "captcha:9f7a6c2d")
    private String captchaKey;

    /**
     * 验证码内容。
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 6, message = "验证码长度必须在4到6之间")
    @Schema(description = "验证码内容", example = "1234")
    private String captchaCode;
}
