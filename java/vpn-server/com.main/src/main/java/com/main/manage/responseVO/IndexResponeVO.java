package com.main.manage.responseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema
public class IndexResponeVO {
    @Schema(description = "平台总用户数量")
    private long platformUsers;
    @Schema(description = "今日注册用户数量")
    private long todayRegisterUsers;
    @Schema(description = "平台会员总数量")
    private long platformMembers;
    @Schema(description = "开通会员总金额")
    private BigDecimal memberAmounts;
    @Schema(description = "到期未续费会员数量")
    private long notContinueMembers;


}
