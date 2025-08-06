package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("system_config")
public class SystemConfig extends AbstractEntity {
    @Schema(description = "配置名称")
    private String systemKey;
    @Schema(description = "配置内容")
    private String systemValue;
    @Schema(description = "配置内容2")
    private String systemValue2;
    @Schema(description = "配置内容3")
    private String systemValue3;
    @Schema(description = "备注")
    private String remark;

}
