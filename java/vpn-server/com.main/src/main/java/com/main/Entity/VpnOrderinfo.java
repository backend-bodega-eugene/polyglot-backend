package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vpn_orderinfo")
public class VpnOrderinfo extends AbstractEntity {
    @Schema(description = "会员id")
    private long memId;
    @Schema(description = "会员名称")
    private String memName;
    @Schema(description = "管理员id")
    private long adminId;
    @Schema(description = "支付id,暂不可用")
    private long paymentId;
    @Schema(description = "支付类型id")
    private long typeId;
    @Schema(description = "支付类型名称")
    private String typeName;
    @Schema(description = "支付通道id")
    private long payId;
    @Schema(description = "支付通道名称")
    private String payName;
    @Schema(description = "套餐id")
    private long mempackageId;
    @Schema(description = "订单id")
    private String orderGuid;
    @Schema(description = "订单金额")
    private BigDecimal orderAmount;
    @Schema(description = "支付地址")
    private String recieveAddress;
    @Schema(description = "订单状态 暂定: 0:处理中 ,1:已完成,2:已取消(包括用户取消),3:已结束(到期后未处理的订单,后台自动处理为已结束)")
    private Integer orderStatus;
    @Schema(description = "订单下单时间")
    private LocalDateTime orderCreatetime;
    @Schema(description = "订单备注")
    private String orderRemark;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "订单天数(套餐的天数)")
    private Integer mempackageQuantity;
    @Schema(description = "上次会员到期时间")
    private LocalDateTime lastMemberdate;
    @Schema(description = "会员到期时间")
    private LocalDateTime expireDate;
    @Schema(description = "统计金额")
    @TableField(exist = false)
    private BigDecimal sumAll;
    @Schema(description = "交易哈希")
    private String hashDeal;
    @Schema(description = "二维码")
    private String recieveQr;
    @Schema(description = "到账时间")
    private LocalDateTime confirmCreatetime;
    @Schema(description = "收款人人名称")
    private String recieveName;
    @Schema(description = "银行名称")
    private String bank;
    @Schema(description = "收款人账号")
    private String recieveAccount;
    @Schema(description = "对应usdt价格")
    private BigDecimal usdt;
    @Schema(description = "协议类型")
    private String recieveProtocol;
    @Schema(description = "用户上传凭证图片")
    private String userImage;
}