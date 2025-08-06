package com.main.manage.requestbodyvo;

import com.alibaba.fastjson.JSONObject;
import com.common.exception.BizException;
import com.common.security.MD5;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
@Data
public class RechargeDto {
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

    public void verifySign(String appId, String appKey) {
        String amount = this.amount.stripTrailingZeros().toPlainString();
        StringBuilder builder = new StringBuilder();
        builder.append("hash=").append(this.hash).append(",");
        builder.append("chainType=").append(this.chainType).append(",");
        builder.append("tokenType=").append(this.tokenType).append(",");
        builder.append("fromAddress=").append(this.fromAddress).append(",");
        builder.append("address=").append(this.address).append(",");
        builder.append("amount=").append(amount).append(",");
        builder.append("appId=").append(appId);
        if (StringUtils.hasText(this.bizId)) {
            builder.append(",bizId=" + this.bizId);
        }
        String sign = MD5.MD5Str(builder.toString(), appKey);
        if (!sign.equals(this.sign)) {
            log.error("充值回调签名失败，参数：{}", JSONObject.toJSONString(this));
            throw new BizException(1, "充值回调签名失败");
        }
    }
}
