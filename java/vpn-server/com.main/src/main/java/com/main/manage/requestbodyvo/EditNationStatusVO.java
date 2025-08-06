package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author langy
 */
@Data
@Schema
public class EditNationStatusVO {
    @Schema(description = "国家id")
    private Long nationid;
    @Schema(description = "状态")
    private Integer status;
}
