package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@Schema
public class VpnPackageRequestVO {
    @Schema(description = "id")
    private long id;
    @Schema(description = "套餐金额")
    private BigDecimal mempackageAmount;
    @Schema(description = "套餐天数")
    private int mempackageQuantity;
    @Schema(description = "排序")
    private Integer sort;
}
