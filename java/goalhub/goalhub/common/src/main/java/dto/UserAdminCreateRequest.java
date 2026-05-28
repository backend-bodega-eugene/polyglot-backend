package dto;

import lombok.Data;

/**
 * 后台应用用户创建请求。
 */
@Data
public class UserAdminCreateRequest {

    /**
     * 用户名。
     */
    private String username;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 登录密码。
     */
    private String password;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像地址。
     */
    private String avatarUrl;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    private Integer status;
}
