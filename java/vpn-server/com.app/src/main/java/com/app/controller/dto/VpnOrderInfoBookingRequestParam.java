package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Schema
public class VpnOrderInfoBookingRequestParam {
//    @NotBlank
//    @Schema(description = "会员id")
//    private long memId;

    @Schema(description = "套餐id")
    private Long mempackageId;
    @Schema(description = "支付通道id")
    private Long payId;
//    @NotBlank
//    @Schema(description = "下单金额")
//    private BigDecimal orderAmount;
//    @NotBlank
//    @Schema(description = "下单地址(目前是trc20地址)")
//    private String paymentAddress;
//    @NotBlank
//    @Schema(description = "套餐相关天数")
//    private Integer mempackageQuantity;
}
