package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PayInfoRequestVO {
    @Schema(description = "页码(第几页)")
    private Long pageIndex;
    @Schema(description = "每页大小")
    private Long pageSize;
    @Schema(description = "通道类型")
    private Long payTypeId;
    @Schema(description = "通道名称")
    private String payInfoName;
}
