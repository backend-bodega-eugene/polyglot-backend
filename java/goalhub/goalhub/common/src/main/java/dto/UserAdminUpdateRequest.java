package dto;

import lombok.Data;

/**
 * 后台应用用户更新请求。
 */
@Data
public class UserAdminUpdateRequest {

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * 密码。
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
