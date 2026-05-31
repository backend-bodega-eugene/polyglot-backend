package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台账户状态修改请求。
 */
@Data
@Schema(description = "后台账户状态修改请求")
public class AdminAccountStatusUpdateRequest {

    /**
     * 账户 ID。
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 账户状态：1 正常，0 禁用。
     */
    @Schema(description = "账户状态:1正常,0禁用")
    private Integer status;
}
