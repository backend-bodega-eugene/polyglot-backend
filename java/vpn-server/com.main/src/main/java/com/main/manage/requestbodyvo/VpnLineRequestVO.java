package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class VpnLineRequestVO {
    @Schema(description = "节点id,编辑时才需要,新增线路不需要传这个参数")
    private Long lineId;
    @Schema(description = "节点名称")
    private String nodeName;
    @Schema(description = "节点图标")
    private String lineIcon;
    @Schema(description = "所属国家")
    private Long lineNationid;
    @Schema(description = "节点状态")
    private Long lineStatus;
    @Schema(description = "节点排序")
    private Integer lineSort;
    @Schema(description = "vmess加密字符串 ")
    private String vmess;
}
