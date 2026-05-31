package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户注册请求。
 */
@Schema(description = "用户注册请求")
@Data
public class RegisterRequest {

    /**
     * 用户名。
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码", example = "123456")
    private String password;

    /**
     * 昵称。
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

}
