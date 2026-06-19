package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除冠军赔率请求")
public class DeleteChampionMarketOddsRequest {

    /**
     * 冠军赔率配置 ID。
     */
    @Schema(description = "冠军赔率配置ID", example = "1")
    private Long id;
}
