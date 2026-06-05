package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台管理员密码更新请求。
 *
 * <p>用于后台重置指定管理员的登录密码。</p>
 */
@Schema(description = "后台管理员密码更新请求")
@Data
public class AdminPasswordUpdateRequest {

    /**
     * 新密码。
     */
    @Schema(description = "新密码", example = "P@ssw0rdDemo")
    @NotBlank(message = "password.cannot.be.empty")
    private String password;
}
