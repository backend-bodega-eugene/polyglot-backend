package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台应用用户更新请求。
 */
@Schema(description = "后台应用用户更新请求")
@Data
public class UserAdminUpdateRequest {

    /**
     * 邮箱。
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号。
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 密码。
     */
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 昵称。
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 头像地址。
     */
    @Schema(description = "头像地址", example = "https://example.com/avatar.png")
    private String avatarUrl;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    private Integer status;
}
