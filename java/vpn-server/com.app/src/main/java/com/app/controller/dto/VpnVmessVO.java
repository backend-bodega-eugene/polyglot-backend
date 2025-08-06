package com.app.controller.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class VpnVmessVO {
    @Schema(description = "节点id")
    @TableId(type = IdType.AUTO)
    private Long lineId;
    @Schema(description = "加密连接字符串 智能模式")
    private String vmess;
    @Schema(description = "加密连接字符串2 全局模式")
    private String vmess2;
}
