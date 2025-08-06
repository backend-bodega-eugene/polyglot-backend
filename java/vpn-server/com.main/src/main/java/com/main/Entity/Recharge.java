package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("vpn_recharge")
public class Recharge extends AbstractEntity {
    /**
     * 链类型
     */
    private String chainType;
    /**
     * 交易hash
     */
    private String hash;
    /**
     * 代币类型
     */
    private String tokenType;
    /**
     * md5签名
     */
    private String sign;
    /**
     * 原地址
     */
    private String fromAddress;
    /**
     * 地址
     */
    private String address;
    /**
     * 金额
     */
    private BigDecimal amount;
    /**
     * 业务id
     */
    private String bizId;
}
