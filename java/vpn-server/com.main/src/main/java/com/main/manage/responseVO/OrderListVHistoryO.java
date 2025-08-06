package com.main.manage.responseVO;

import com.main.Entity.VpnOrderinfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema
public class OrderListVHistoryO {
    @Schema(description = "取消金额")
    private BigDecimal cancel;
    @Schema(description = "完成金额")
    private BigDecimal success;
    @Schema(description = "总金额")
    private List<VpnOrderinfo> list;
}
