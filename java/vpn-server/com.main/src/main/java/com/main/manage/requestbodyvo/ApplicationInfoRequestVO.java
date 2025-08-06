package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class ApplicationInfoRequestVO {
    @Schema(description = "图标")

    private String applicationIcon;
    @Schema(description = "名称")
    private String applicationName;
    @Schema(description = "下载地址")
    private String applicationAddress;
    @Schema(description = "状态")
    private Integer applicationStatus;
    @Schema(description = "客户端id,编辑时才需要,新增套餐不需要传这个参数")
    private long id;
}
