package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台账户状态修改请求。
 *
 * <p>用于后台启用或禁用指定用户账户。</p>
 */
@Data
@Schema(description = "后台账户状态修改请求")
public class AdminAccountStatusUpdateRequest {

    /**
     * 账户 ID。
     */
    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户 ID", example = "20001")
    private Long accountId;

    /**
     * 账户状态：1 正常，0 禁用。
     */
    @NotNull(message = "账户状态不能为空")
    @Min(value = 0, message = "账户状态只能是0或1")
    @Max(value = 1, message = "账户状态只能是0或1")
    @Schema(description = "账户状态：1 正常，0 禁用", example = "1")
    private Integer status;
}
