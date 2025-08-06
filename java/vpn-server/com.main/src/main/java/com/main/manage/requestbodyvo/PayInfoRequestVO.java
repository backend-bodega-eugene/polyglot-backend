package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PayInfoRequestVO {
    @Schema(description = "页码(第几页)")
    private Long pageIndex = 1L;
    @Schema(description = "每页大小")
    private Long pageSize = 10L;
    @Schema(description = "通道类型")
    private Long typeId;
    @Schema(description = "通道名称")
    private String payName;
    @Schema(description = "通道类型")
    private Boolean status;
}
