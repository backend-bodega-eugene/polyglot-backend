package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class OrderRemakrParamVO {
    @Schema(description = "订单id")
    private Long id;
    @Schema(description = "备注")
    private String remark;
}
