package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台应用用户创建请求。
 *
 * <p>用于后台创建应用用户并初始化联系方式、密码、昵称、头像和账号状态。</p>
 */
@Schema(description = "后台应用用户创建请求")
@Data
public class UserAdminCreateRequest {

    /**
     * 用户名。
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3到50之间")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 邮箱。
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号。
     */
    @Size(max = 20, message = "手机号长度不能超过20")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 登录密码。
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须在6到64之间")
    @Schema(description = "登录密码", example = "P@ssw0rdDemo")
    private String password;

    /**
     * 昵称。
     */
    @Size(max = 50, message = "昵称长度不能超过50")
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 头像地址。
     */
    @Size(max = 255, message = "头像地址长度不能超过255")
    @Schema(description = "头像地址", example = "https://example.com/avatar.png")
    private String avatarUrl;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态只能是0或1")
    @Max(value = 1, message = "账号状态只能是0或1")
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    private Integer status;
}
