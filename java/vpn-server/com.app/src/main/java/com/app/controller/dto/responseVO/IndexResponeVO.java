package com.app.controller.dto.responseVO;

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

    //新的
//    @Schema(description = "搜索时间段内，充值成功的金额")
//    private BigDecimal topUpAmoutBytime;
//    @Schema(description = "站点当前有效会员数")
//    private Long validMemeber;
//    @Schema(description = "当前站点游客数")
//    private Long visitTotal;
//    @Schema(description = "当前站点所有用户数")
//    private Long allMember;
//    @Schema(description = "所有设备登录的次数。 数据去重")
//    private Long loginTotal;
//    @Schema(description = "搜索时间内，成功支付订单的笔数")
//    private Long successTotalCount;
//    @Schema(description = "搜索时间内，充值成功的人数")
//    private Long orderTotalPersons;
////    @Schema(description = "搜索时间内，充值过但没有会员使用时间的用户")
////    private BigDecimal orderTotalPersons;
//    @Schema(description = "搜索时间段内，搜索时间内，充值过但没有会员使用时间的用户")
//    private Long expireMember;

}
