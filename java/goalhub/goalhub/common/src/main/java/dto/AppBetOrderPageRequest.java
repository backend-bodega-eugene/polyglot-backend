package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppBetOrderPageRequest {

    private Integer pageIndex = 1;

    private Integer pageSize = 10;

    private String langCode = "zh-CN";

    /**
     * 关键字：订单号、玩法名称、选项名称。
     */
    private String keywords;

    /**
     * 下单开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 下单结束时间。
     */
    private LocalDateTime endTime;
}