package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema
public class IdDTOVO {
    @NotNull(message = "支付通道id不能为空")
    @Schema(description = "id")
    private Long id;
}
