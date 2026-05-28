package dto;

import lombok.Data;

/**
 * 后台管理员密码更新请求。
 */
@Data
public class AdminPasswordUpdateRequest {

    /**
     * 新密码。
     */
    private String password;
}
