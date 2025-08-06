package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class EditpPasswordVO {
    @Schema(description = "管理员id")
    private long adminId;
    @Schema(description = "旧密码")
    private String oldPassword;
    @Schema(description = "新密码")
    private String newPassword;
}
