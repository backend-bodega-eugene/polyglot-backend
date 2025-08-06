package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class AdminLoginVO {
    @Schema(description = "登录名")
    private String adminName;
    @Schema(description = "密码")
    private String adminPassword;
    @Schema(description = "验证码")
    private String vCode;
}
