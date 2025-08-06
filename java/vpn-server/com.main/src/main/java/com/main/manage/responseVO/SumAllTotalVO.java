package com.main.manage.responseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema
public class SumAllTotalVO {
    @Schema(description = "支付类型id值")
    private BigDecimal success;
    @Schema(description = "支付类型文字")
    private BigDecimal cancel;
}
