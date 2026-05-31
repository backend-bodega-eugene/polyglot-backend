package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台删除球队请求。
 */
@Schema(description = "后台删除球队请求")
@Data
public class DeleteTeamRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID")
    private Long id;
}
