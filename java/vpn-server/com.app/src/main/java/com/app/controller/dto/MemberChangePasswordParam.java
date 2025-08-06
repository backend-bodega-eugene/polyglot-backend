package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema
public class MemberChangePasswordParam {
    @NotBlank(message = "必须输入账号")
    @Schema(description = "账号")
    private String memberAccount;
    @NotBlank(message = "必须输入密保问题")
    @Schema(description = "密保问题")
    private String passwordProblem;
    @NotBlank(message = "必须输入密码,长度 8到16位,只能包含数字和字母")
    @Schema(description = "密码")
    private String memberPassword;
}
