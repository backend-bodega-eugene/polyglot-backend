package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema
public class PayInfoAmountVO {

    @Schema(description = "金额")
    private BigDecimal amount;
}
