package com.main.controller;

import com.alibaba.fastjson.JSONObject;
import com.common.util.BeanCoper;
import com.main.Entity.Recharge;
import com.main.manage.EnvProperties;
import com.main.manage.requestbodyvo.RechargeDto;
import com.main.service.RechargeService;
import io.swagger.annotations.Api;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Api(value = "区块到账接口", tags = {"区块到账接口"})
@RestController
@RequestMapping(value = "/backend/recharge", method = {RequestMethod.POST})
public class RechargeController {
    @Autowired
    RechargeService rechargeService;
    @Resource
    private EnvProperties envProperties;

    /**
     * 充值
     * amount 除去小数尾0，md5签名顺序 hash=hash,chainType=ETH,tokenType=USDT,address=xxxxx,amount=100
     *
     * @param dto
     */
    @RequestMapping(value = "/recharge")
    public void recharge(@RequestBody RechargeDto dto) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        var request = servletRequestAttributes.getRequest();
        String appId = request.getHeader("appId");
        log.info(appId);
        log.info(JSONObject.toJSONString(dto));
        dto.verifySign(appId, envProperties.getAppKey());
        Recharge recharge = BeanCoper.clone(Recharge.class, dto);
        rechargeService.save(recharge);
    }

}
