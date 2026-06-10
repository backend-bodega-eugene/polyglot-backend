package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "找回密码重置密码请求")
public class ForgotPasswordResetRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    @Schema(description = "邮箱", example = "test@gmail.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 10, message = "验证码长度不正确")
    @Schema(description = "邮箱验证码", example = "123456")
    private String code;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 64, message = "新密码长度必须在6到64之间")
    @Schema(description = "新密码", example = "P@ssw0rdDemo")
    private String newPassword;
}