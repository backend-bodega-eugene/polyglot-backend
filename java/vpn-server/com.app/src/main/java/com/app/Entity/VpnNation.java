package com.app.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("vpn_nation")
public class VpnNation extends AbstractEntity {
    @Schema(description = "国家名称")
    private String nationName;
    @Schema(description = "国家图标")
    private String nationIcon;
    @Schema(description = "国家状态")
    private Integer nationStatus;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "备注")
    private String remark;
}
