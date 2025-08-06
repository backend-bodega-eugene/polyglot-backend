package com.main.manage.requestbodyvo;

import com.common.validation.TextDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Schema
public class OderQuestParam {

    @Schema(description = "页码(第几页)")
    private Long pageIndex = 1L;
    @Schema(description = "每页大小")
    private Long pageSize = 10L;
    @Schema(description = "订单编号")
    private String orderGuid;
    @Min(value = 1, message = "必须是数字,没有为0的会员")
    @Schema(description = "会员id")
    private String memId = "";
    @Schema(description = "用户名")
    private String memName;
    @Schema(description = "充值渠道")
    private Long[] payIds;
    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDatetime = LocalDateTime.now().minusYears(50);
    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDatetime = LocalDateTime.now();

}
