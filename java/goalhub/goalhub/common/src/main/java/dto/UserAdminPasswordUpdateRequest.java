package dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台应用用户密码更新请求。
 *
 * <p>用于后台为应用用户重置或更新登录密码。</p>
 */
@Schema(description = "后台应用用户密码更新请求")
@Data
public class UserAdminPasswordUpdateRequest {

    /**
     * 新密码。
     */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "新密码长度必须在6到64之间")
    @Schema(description = "新密码", example = "P@ssw0rdDemo")
    private String password;
}
