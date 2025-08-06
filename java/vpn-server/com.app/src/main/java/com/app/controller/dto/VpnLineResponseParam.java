package com.app.controller.dto;

import com.app.Entity.IBaseEntity;
import com.app.Entity.VpnLine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema
public class VpnLineResponseParam implements IBaseEntity {

    @Schema(description = "国家id")
    private long nationId;
    @Schema(description = "会员密码")
    private String nationName;
    @Schema(description = "国家描述")
    private String nationDescription;
    @Schema(description = "其他,暂不使用")
    private String vCode;
    @Schema(description = "国家所属节点")
    private List<VpnVO> nationVpnLine;
}
