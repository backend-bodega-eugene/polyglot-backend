package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_info")
public class PayInfo extends AbstractEntity {
    @Schema(description = "支付类型")
    private Long typeId;
    @Schema(description = "通道名称")
    private String payName;
    @Schema(description = "收款人人名称")
    private String recieveName;
    @Schema(description = "收款人账号")
    private String recieveAccount;
    @Schema(description = "银行名称")
    private String bank;
    @Schema(description = "收款协议")
    private String recieveProtocol;
    @Schema(description = "收款二维码")
    private String recieveQr;
    @Schema(description = "收款地址")
    private String recieveAddress;
    @Schema(description = "通道提示")
    private String payTitle;
    @Schema(description = "最小金额")
    private BigDecimal lowLimit;
    @Schema(description = "最大金额")
    private BigDecimal highLimit;
    @Schema(description = "创建时间")
    private LocalDateTime createTime = LocalDateTime.now();
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "通道状态")
    private Boolean status;
}
