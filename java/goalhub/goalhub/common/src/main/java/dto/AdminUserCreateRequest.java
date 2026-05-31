package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台管理员创建请求。
 */
@Schema(description = "后台管理员创建请求")
@Data
public class AdminUserCreateRequest {

    /**
     * 管理员账号。
     */
    @Schema(description = "管理员账号", example = "admin")
    private String username;

    /**
     * 登录密码。
     */
    @Schema(description = "登录密码", example = "123456")
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
    private Integer isSuperAdmin;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    private Integer status;
}
