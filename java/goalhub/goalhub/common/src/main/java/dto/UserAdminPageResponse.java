package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台应用用户分页响应。
 *
 * <p>返回后台用户列表中展示的应用用户基础资料、状态和时间信息。</p>
 */
@Schema(description = "后台应用用户分页响应")
@Data
public class UserAdminPageResponse {

    /**
     * 应用用户 ID。
     */
    @Schema(description = "应用用户 ID", example = "1")
    private Long id;

    /**
     * 用户名。
     */
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 邮箱。
     */
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号。
     */
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 昵称。
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;

    /**
     * 头像地址。
     */
    @Schema(description = "头像地址", example = "https://example.com/avatar.png")
    private String avatarUrl;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    private Integer status;

    /**
     * 最近登录时间。
     */
    @Schema(description = "最近登录时间", example = "2026-05-30T12:00:00")
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间", example = "2026-05-30T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间", example = "2026-05-30T12:00:00")
    private LocalDateTime updatedAt;
}
