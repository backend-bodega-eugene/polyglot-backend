package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class ClientUpgradeQueryVO {
    @Schema(description = "页码(第几页)")
    private long pageIndex;
    @Schema(description = "每页大小")
    private long pageSize;
    @Schema(description = "版本名称")
    private String versionName;
    @Schema(description = "客户端名称")
    private String client;
    @Schema(description = "状态")
    private Integer status;
}
