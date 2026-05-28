package dto;

import lombok.Data;

/**
 * 后台管理员创建请求。
 */
@Data
public class AdminUserCreateRequest {

    /**
     * 管理员账号。
     */
    private String username;

    /**
     * 登录密码。
     */
    private String password;

    /**
     * 管理员昵称。
     */
    private String nickname;

    /**
     * 是否超级管理员：1 是，0 否。
     */
    private Integer isSuperAdmin;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    private Integer status;
}
