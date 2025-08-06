package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class MemberRequestParamv2 extends MemberRequestParam {
    @Schema(description = "设备id")
    private String deviceId;
}
