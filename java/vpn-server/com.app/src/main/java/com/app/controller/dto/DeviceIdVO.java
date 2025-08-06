package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema
public class DeviceIdVO {
    @NotBlank(message = "必须输入设备id")
    @Schema(description = "设备id")
    private String deviceId;
}
