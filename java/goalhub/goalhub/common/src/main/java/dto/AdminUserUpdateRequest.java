package dto;

import lombok.Data;

/**
 * 后台管理员更新请求。
 */
@Data
public class AdminUserUpdateRequest {

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
}
