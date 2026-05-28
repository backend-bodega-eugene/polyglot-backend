package dto;

import lombok.Data;

/**
 * 用户注册请求。
 */
@Data
public class RegisterRequest {

    /**
     * 用户名。
     */
    private String username;

    /**
     * 登录密码。
     */
    private String password;

    /**
     * 昵称。
     */
    private String nickname;

}
