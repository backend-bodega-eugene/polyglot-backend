package com.app.controller.dto.responsedto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class SpeedstatusVO {
    @Schema(description = "会员id")
    private long memid;
    @Schema(description = "会员名称")
    private String memName;
    @Schema(description = "会员连接状态,开启,还是关闭")
    private int status;
}
