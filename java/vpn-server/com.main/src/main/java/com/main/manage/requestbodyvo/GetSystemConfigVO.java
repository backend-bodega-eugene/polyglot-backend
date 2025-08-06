package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class GetSystemConfigVO {
    @Schema(description = "配置名称 查询的时候传入名称就可以得到对应值")
    private String systemconfigName;
    @Schema(description = "配置内容")
    private String systemconfigValue;
}
