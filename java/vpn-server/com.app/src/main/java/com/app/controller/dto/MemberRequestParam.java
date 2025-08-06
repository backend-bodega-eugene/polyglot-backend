package com.app.controller.dto;

import com.app.Entity.IBaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema
public class MemberRequestParam implements IBaseEntity {
    @NotBlank(message = "必须输入账号")
    @Schema(description = "账号(手机号码或者邮件)")
    private String memberAccount;
    @NotBlank(message = "必须输入密码")
    @Schema(description = "会员密码")
    private String memberPassword;
    @Schema(description = "邀请码可选")
    private String iCode;
    @NotBlank(message = "密保问题必须输入")
    @Schema(description = "密保问题")
    private String ProtectPassword;
//    @NotBlank
//    @Schema(description = "验证码")
//    private String vCode;
}
