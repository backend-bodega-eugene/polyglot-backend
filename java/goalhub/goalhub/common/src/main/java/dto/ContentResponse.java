package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 内容响应。
 *
 * <p>返回后台或 App 端展示内容所需的标题、摘要、正文、状态和发布时间。</p>
 */
@Schema(description = "内容响应")
@Data
public class ContentResponse {

    /**
     * 内容 ID。
     */
    @Schema(description = "内容 ID", example = "1")
    private Long id;

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

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
