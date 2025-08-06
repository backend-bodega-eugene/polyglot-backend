package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class EditLineStatusVO {
    @Schema(description = "节点id")
    private long lineId;
    @Schema(description = "状态")
    private Integer status;

}
