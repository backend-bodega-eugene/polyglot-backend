package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台内容分页查询请求。
 *
 * <p>用于后台按内容类型、状态和关键词分页查询内容列表。</p>
 */
@Schema(description = "后台内容分页查询请求")
@Data
public class AdminContentPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex = 1;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    /**
     * 内容类型。
     */
    @Schema(description = "内容类型", example = "NOTICE")
    private String type;

    /**
     * 内容状态。
     */
    @Schema(description = "内容状态", example = "PUBLISHED")
    private String status;

    /**
     * 关键词。
     */
    @Schema(description = "关键词")
    private String keyword;
}
