package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PageResultRequestVOByLine {
    @Schema(description = "页码(第几页)")
    private long pageIndex;
    @Schema(description = "每页大小")
    private long pageSize;
    @Schema(description = "查询条件:节点名称")
    private String nodeName;
    @Schema(description = "状态:0,启用,1禁用")
    private Integer lineStatus;
}
