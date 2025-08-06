package com.app.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("vpn_line")
public class VpnLine {
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
    private Integer sort;
    @Schema(description = "端口")
    private String port;
    @Schema(description = "主机")
    private String host;
    @Schema(description = "加密")
    @JsonProperty()
    private String vmess;
    @Schema(description = "加密2")
    @JsonProperty()
    private String vmess2;

}
