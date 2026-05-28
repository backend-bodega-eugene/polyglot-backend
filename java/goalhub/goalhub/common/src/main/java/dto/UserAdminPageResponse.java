package dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台应用用户分页响应。
 */
@Data
public class UserAdminPageResponse {

    /**
     * 应用用户 ID。
     */
    private Long id;

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

    /**
     * 最近登录时间。
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
