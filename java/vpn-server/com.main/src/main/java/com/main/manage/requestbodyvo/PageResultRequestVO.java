package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PageResultRequestVO {
    @Schema(description = "页码(第几页)")
    private long pageIndex;
    @Schema(description = "每页大小")
    private long pageSize;
    @Schema(description = "查询条件:国家名称")
    private String nationName;
    @Schema(description = "状态:0,启用,1禁用")
    private Integer nationStatus;
}
