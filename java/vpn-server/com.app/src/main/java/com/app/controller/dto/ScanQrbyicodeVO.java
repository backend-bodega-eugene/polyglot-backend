package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class ScanQrbyicodeVO {
    @Schema(description = "会员id")
    private long memid;
    @Schema(description = "邀请码")
    private String invitationCode;
    @Schema(description = "设备id")
    private String deviceId;
}
