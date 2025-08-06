package com.app.controller.dto.responsedto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class BindUserInfoVO {
    @Schema(description = "会员id(游客)")
    private long memId;
    @Schema(description = "要绑定的账户(邮箱或者手机号码)")
    private String accountName;
    @Schema(description = "用户密码")
    private String passWord;
    @Schema(description = "验证码(暂时不用)")
    private String vcode;
}
