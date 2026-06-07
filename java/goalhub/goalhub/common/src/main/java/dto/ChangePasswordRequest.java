package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求。
 *
 * <p>用于用户或管理员提交旧密码和新密码完成密码修改。</p>
 */
@Schema(description = "修改密码请求")
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码。
     */
    @Schema(description = "旧密码")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码。
     */
    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
