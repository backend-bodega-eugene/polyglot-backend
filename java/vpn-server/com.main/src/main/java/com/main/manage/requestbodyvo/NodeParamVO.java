package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema
public class NodeParamVO {
    @NotBlank
    @Schema(description = "节点ip")
    private String ip;
    @NotBlank
    @Schema(description = "节点端口")
    private String port;
    @NotBlank
    @Schema(description = "节点名称")
    private String name;
}
