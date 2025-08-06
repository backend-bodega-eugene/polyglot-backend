package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema
public class ChangePasswordRequestVO {
    //    @Schema(description = "用户id")
//    private Long id;
    @NotBlank
    @Schema(description = "验证码")
    private String oldPassword;
    @NotBlank
    @Schema(description = "密码")
    private String newPassword;
}
