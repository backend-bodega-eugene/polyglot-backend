package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户登录响应。
 */
@Schema(description = "用户登录响应")
@Data
public class LoginResponse {

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "1")
    private Long userId;

    /**
     * 用户名。
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 登录成功后签发的访问令牌。
     */
    @Schema(description = "登录成功后签发的访问令牌")
    private String token;
}
