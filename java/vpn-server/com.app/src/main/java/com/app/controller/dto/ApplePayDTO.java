package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ApplePayDTO {
    @Schema(description = "用户ID")
    private String userId;
    @Schema(description = "苹果传递前端给的值")
    private String receipt;
    @Schema(description = "是否时测试环境")
    private Boolean chooseEnv;
}
