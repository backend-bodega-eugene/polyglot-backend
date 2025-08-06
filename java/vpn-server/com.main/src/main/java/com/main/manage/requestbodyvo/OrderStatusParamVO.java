package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class OrderStatusParamVO {
    @Schema(description = "订单id")
    private Long id;


}
