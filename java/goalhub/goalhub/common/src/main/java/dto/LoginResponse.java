package dto;

import lombok.Data;

/**
 * 用户登录响应。
 */
@Data
public class LoginResponse {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 登录成功后签发的访问令牌。
     */
    private String token;
}
