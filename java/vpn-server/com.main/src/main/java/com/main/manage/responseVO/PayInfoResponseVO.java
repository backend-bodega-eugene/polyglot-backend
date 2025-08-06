package com.main.manage.responseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PayInfoResponseVO {
    @Schema(description = "支付通道id值")
    private Long value;
    @Schema(description = "支付通道文字")
    private String label;
}
