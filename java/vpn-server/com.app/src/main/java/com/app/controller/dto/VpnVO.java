package com.app.controller.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class VpnVO {
    @Schema(description = "节点id")
    @TableId(type = IdType.AUTO)
    private Long lineId;
    @Schema(description = "国家id")
    private Long lineNationid;
    @Schema(description = "节点名称")
    private String lineName;
    @Schema(description = "节点图标")
    private String lineIcon;
    @Schema(description = "节点状态")
    private Integer lineStatus;
    @Schema(description = "排序")
    private String sort;
    @Schema(description = "主机")
    private String host;
    @Schema(description = "端口")
    private String port;
}
