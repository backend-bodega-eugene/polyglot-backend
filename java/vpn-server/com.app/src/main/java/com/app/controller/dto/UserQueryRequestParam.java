package com.app.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema
public class UserQueryRequestParam {

    //传0,1,2的时候,可以不传两个时间 ,状态可以不传,就是全部
    @Schema(description = "0:今日,1:近7日,2:近30日,3:自定义")
    private int type = -1;
    @Schema(description = "起始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDatetime;
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDatetime;
    @Schema(description = "订单状态 暂定: 0,未支付,1,审核通过,2,冻结,3异常,4,推迟,5,拒绝,6取消")
    private int orderStatus = -1;
}
