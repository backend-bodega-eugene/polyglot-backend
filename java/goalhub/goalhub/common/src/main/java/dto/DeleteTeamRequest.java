package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台删除球队请求。
 *
 * <p>根据球队 ID 删除指定球队记录。</p>
 */
@Schema(description = "后台删除球队请求")
@Data
public class DeleteTeamRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID", example = "1")
    private Long id;
}
