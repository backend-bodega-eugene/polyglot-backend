package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台管理员登录响应。
 *
 * <p>登录成功后返回管理员基础信息和后台访问令牌。</p>
 */
@Schema(description = "后台管理员登录响应")
@Data
public class AdminLoginResponse {

    /**
     * 管理员 ID。
     */
    @Schema(description = "管理员 ID", example = "1")
    private Long id;

    /**
     * 管理员账号。
     */
    @Schema(description = "管理员账号", example = "admin")
    private String username;

    /**
     * 管理员昵称。
     */
    @Schema(description = "管理员昵称", example = "超级管理员")
    private String nickname;

    /**
     * 是否超级管理员。
     */
    @Schema(description = "是否超级管理员，1 是，0 否", example = "1")
    private Integer isSuperAdmin;

    /**
     * 登录成功后签发的后台访问令牌。
     */
    @Schema(description = "登录成功后签发的后台访问令牌")
    private String token;
}
