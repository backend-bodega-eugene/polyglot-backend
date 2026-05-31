package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台管理员密码更新请求。
 */
@Schema(description = "后台管理员密码更新请求")
@Data
public class AdminPasswordUpdateRequest {

    /**
     * 新密码。
     */
    @Schema(description = "新密码", example = "123456")
    private String password;
}
