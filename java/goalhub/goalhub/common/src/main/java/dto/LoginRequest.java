package dto;

import lombok.Data;

/**
 * 用户登录请求。
 */
@Data
public class LoginRequest {

    /**
     * 登录账号，可为用户名、邮箱或手机号。
     */
    private String account;

    /**
     * 登录密码。
     */
    private String password;
}
