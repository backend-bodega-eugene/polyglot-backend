package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户注册请求。
 */
@Schema(description = "用户注册请求")
@Data
public class RegisterRequest {

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "登录密码", example = "123456")
    private String password;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "验证码 key")
    private String captchaKey;

    @Schema(description = "验证码内容", example = "1234")
    private String captchaCode;
}