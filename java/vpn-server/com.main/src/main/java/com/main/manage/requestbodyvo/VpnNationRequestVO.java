package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class VpnNationRequestVO {
    @Schema(description = "国家名称")
    private String nationName;
    @Schema(description = "国家图标")
    private String nationIcon;
    @Schema(description = "国家状态")
    private Integer nationStatus;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "国家id,编辑时才需要,新增国家不需要传这个参数")
    private long id;
}
