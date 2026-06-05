package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台删除联赛请求。
 *
 * <p>根据联赛 ID 删除指定联赛记录。</p>
 */
@Schema(description = "后台删除联赛请求")
@Data
public class DeleteLeagueRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long id;
}
