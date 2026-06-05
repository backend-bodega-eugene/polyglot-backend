package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求。
 *
 * <p>提交用户名、密码、昵称和图形验证码信息完成用户注册。</p>
 */
@Schema(description = "用户注册请求")
@Data
public class RegisterRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50之间")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 登录密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在6到64之间")
    @Schema(description = "登录密码", example = "P@ssw0rdDemo")
    private String password;

    /**
     * 昵称。
     */
    @Size(max = 50, message = "昵称长度不能超过50")
    @Schema(description = "昵称", example = "张三")
    private String nickname;

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
