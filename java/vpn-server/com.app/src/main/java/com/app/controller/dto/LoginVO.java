package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema
public class LoginVO {
    @NotBlank(message = "会员账号")
    @Schema(description = "会员账号")
    private String memberName;
    @NotBlank(message = "会员密码")
    @Schema(description = "会员密码")
    private String memberPassword;
    @Schema(description = "设备id")
    private String deviceId;
}
