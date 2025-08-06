package com.main.manage.responseVO;

import com.main.Entity.MemberRechargeflow;
import com.main.Entity.VpnOrderinfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema
public class MemberRechargeflowVO {
    @Schema(description = "总金额")
    private long total;
    @Schema(description = "总金额")
    private List<MemberRechargeflow> list;
}
