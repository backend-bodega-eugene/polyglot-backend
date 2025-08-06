package com.main.Entity;

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
    @TableField("`port`")
    private String port;
    @Schema(description = "ps描述名")
    private String ps;
    @Schema(description = "断点")
    private String tis;
    @Schema(description = "机器id")
    @TableField("`id`")
    private String id;
    @Schema(description = "帮助id")
    private String aid;
    @Schema(description = "识别")
    private String v;
    @Schema(description = "主机")
    @TableField("`host`")
    private String host;
    @Schema(description = "类型")
    @TableField("`type`")
    private String type;
    @Schema(description = "路径")
    private String path;
    @Schema(description = "网络")
    private String net;
    @Schema(description = "地址")
    @TableField("`add`")
    private String add;
    @Schema(description = "加密")
    private String vmess;
    @Schema(description = "加密2")
    @JsonProperty()
    private String vmess2;
    @Schema(description = "速度")
    @TableField(exist = false)
    private String speed;
}
