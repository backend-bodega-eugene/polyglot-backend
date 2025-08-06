package com.main.manage.responseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema
public class VpnPackageVO {
    @Schema(description = "id")
    private long id;
    @Schema(description = "套餐金额")
    private BigDecimal mempackageAmount;
    @Schema(description = "套餐天数")
    private int mempackageQuantity;
    @Schema(description = "套餐货币,目前默认是rmb")
    private String mempackageCurrency = "rmb";
    @Schema(description = "套餐状态 ,是否可用")
    private Integer mempackageStatus;
    @Schema(description = "排序")
    private Integer sort;

}
