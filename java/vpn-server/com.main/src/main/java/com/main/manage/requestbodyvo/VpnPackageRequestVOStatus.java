package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class VpnPackageRequestVOStatus {
    @Schema(description = "id")
    private long id;
    @Schema(description = "套餐状态 ,是否可用")
    private Integer mempackageStatus;
}
