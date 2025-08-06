package com.app.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vpn_payment")
public class VpnPayment extends AbstractEntity {
    @Schema(description = "支付名称")
    private String paymentName;
    @Schema(description = "支付类型")
    private Integer paymentType;
    @Schema(description = "支付地址")
    private String paymentAddress;
    @Schema(description = "支付key")
    private String paymentKey;
    @Schema(description = "支付appid")
    private String paymentAppid;
    @Schema(description = "支付地址扩展 暂时不可用")
    private String paymentAddressextend;
    @Schema(description = "支付回调地址")
    private String paymentCallbackurl;
    @Schema(description = "支付二维码")
    private String paymentQr;
    @Schema(description = "支付货币")
    private String paymentCurrency;
    @Schema(description = "支付产品状态")
    private Integer paymentStatus;
    @Schema(description = "培训")
    private Integer sort;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "备注")
    private String remark;
}
