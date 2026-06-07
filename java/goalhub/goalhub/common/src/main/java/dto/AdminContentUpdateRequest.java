package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台内容更新请求。
 *
 * <p>用于后台更新公告、文章或其他运营内容。</p>
 */
@Schema(description = "后台内容更新请求")
@Data
public class AdminContentUpdateRequest {

    /**
     * 内容类型。
     */
    @Schema(description = "内容类型", example = "NOTICE")
    private String type;

    /**
     * 内容标题。
     */
    @Schema(description = "内容标题")
    private String title;

    /**
     * 内容摘要。
     */
    @Schema(description = "内容摘要")
    private String summary;

    /**
     * 封面图片地址。
     */
    @Schema(description = "封面图片地址")
    private String coverUrl;

    /**
     * HTML 格式正文。
     */
    @Schema(description = "HTML 格式正文")
    private String contentHtml;

    /**
     * 内容状态。
     */
    @Schema(description = "内容状态", example = "PUBLISHED")
    private String status;

    /**
     * 排序值。
     */
    @Schema(description = "排序值", example = "10")
    private Integer sort;

    /**
     * 发布时间。
     */
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
}
