package com.main.manage.requestbodyvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema
public class GetAllFlowsExtendVO extends GetAllFlowsVO {

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDatetime = LocalDateTime.now().minusYears(50);
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDateTime = LocalDateTime.now();
    @Schema(description = "奖励条件")
    private Integer opId = -1;
    @Schema(description = "用户id")
    private long memId = -1;
    @Schema(description = "订单id")
    private String id;
    @Schema(description = "用户名")
    private String memName;
}
