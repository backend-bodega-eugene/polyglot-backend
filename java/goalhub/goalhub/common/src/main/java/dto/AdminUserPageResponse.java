package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台管理员分页响应。
 */
@Schema(description = "后台管理员分页响应")
@Data
public class AdminUserPageResponse {

    /**
     * 管理员 ID。
     */
    @Schema(description = "管理员 ID", example = "1")
    private Long id;

    /**
     * 管理员账号。
     */
    @Schema(description = "管理员账号", example = "admin")
    private String username;

    /**
     * 管理员昵称。
     */
    @Schema(description = "管理员昵称", example = "超级管理员")
    private String nickname;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    @Schema(description = "账号状态：1 启用，0 禁用", example = "1")
    private Integer status;

    /**
     * 是否超级管理员：1 是，0 否。
     */
    @Schema(description = "是否超级管理员：1 是，0 否", example = "1")
    private Integer isSuperAdmin;

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
