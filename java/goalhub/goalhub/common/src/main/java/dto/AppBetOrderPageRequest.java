package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App 投注订单分页查询请求。
 *
 * <p>用于 App 端按关键词和下单时间范围分页查询当前用户的投注订单。</p>
 */
@Schema(description = "App 投注订单分页查询请求")
@Data
public class AppBetOrderPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex = 1;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode = "zh-CN";

    /**
     * 关键字：订单号、玩法名称、选项名称。
     */
    @Schema(description = "关键字：订单号、玩法名称、选项名称")
    private String keywords;

    /**
     * 下单开始时间。
     */
    @Schema(description = "下单开始时间")
    private LocalDateTime startTime;

    /**
     * 下单结束时间。
     */
    @Schema(description = "下单结束时间")
    private LocalDateTime endTime;
}
