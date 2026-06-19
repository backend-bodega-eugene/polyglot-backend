package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 冠军赔率快照查询请求。
 *
 * <p>用于下单前按冠军赔率 ID 查询当前可投注快照。</p>
 */
@Data
@Schema(description = "冠军赔率快照查询请求")
public class ChampionOddsSnapshotRequest {

    /**
     * 冠军赔率 ID。
     */
    @Schema(description = "冠军赔率ID", example = "1")
    private Long championOddsId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "en-US")
    private String langCode="en-US";
}
