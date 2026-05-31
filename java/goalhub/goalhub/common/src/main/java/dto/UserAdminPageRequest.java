package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台应用用户分页查询请求。
 */
@Schema(description = "后台应用用户分页查询请求")
@Data
public class UserAdminPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    /**
     * 用户名筛选条件。
     */
    @Schema(description = "用户名筛选条件", example = "zhangsan")
    private String username;

    /**
     * 昵称筛选条件。
     */
    @Schema(description = "昵称筛选条件", example = "张三")
    private String nickname;

    /**
     * 邮箱筛选条件。
     */
    @Schema(description = "邮箱筛选条件", example = "zhangsan@example.com")
    private String email;

    /**
     * 手机号筛选条件。
     */
    @Schema(description = "手机号筛选条件", example = "13800138000")
    private String phone;

    /**
     * 账号状态筛选条件。
     */
    @Schema(description = "账号状态筛选条件：1 启用，0 禁用", example = "1")
    private Integer status;

    /**
     * 创建时间起始范围。
     */
    @Schema(description = "创建时间起始范围", example = "2026-05-30T00:00:00")
    private LocalDateTime createdAtStart;

    /**
     * 创建时间结束范围。
     */
    @Schema(description = "创建时间结束范围", example = "2026-05-30T23:59:59")
    private LocalDateTime createdAtEnd;
}
