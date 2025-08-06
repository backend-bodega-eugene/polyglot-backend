package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member_rechargeflow")
public class MemberRechargeflow extends AbstractEntity {
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "操作")
    private String op;
    @Schema(description = "是否会员")
    private Integer ismember;
    @Schema(description = "上次到期时间")
    private LocalDateTime lastExpiretime;
    @Schema(description = "天数")
    private long days;
    @Schema(description = "变动后到期时间")
    private LocalDateTime newExpiretime;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "交易哈希")
    private String hashDeal;
    @Schema(description = "会员id")
    private long memId;
    @Schema(description = "会员名称")
    private String memName;
    @Schema(description = "操作id")
    private long opId;
    @Schema(description = "订单id")
    private String orderId;


}
