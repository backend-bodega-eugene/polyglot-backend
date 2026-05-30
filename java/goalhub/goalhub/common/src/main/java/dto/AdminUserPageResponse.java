package dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员分页响应。
 */
@Data
public class AdminUserPageResponse {

    /**
     * 管理员 ID。
     */
    private Long id;

    /**
     * 管理员账号。
     */
    private String username;

    /**
     * 管理员昵称。
     */
    private String nickname;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    private Integer status;

    /**
     * 是否超级管理员：1 是，0 否。
     */
    private Integer isSuperAdmin;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
