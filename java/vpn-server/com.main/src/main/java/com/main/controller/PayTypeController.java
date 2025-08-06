package com.main.controller;

import com.main.Entity.PayType;
import com.main.manage.responseVO.PayTypeResponseVO;
import com.main.service.PayTypeService;
import com.main.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Api(value = "支付类型", tags = {"支付类型查询"})
@RestController
@RequestMapping(value = "/backend/paytype", method = {RequestMethod.POST})
public class PayTypeController {


    @Autowired
    PayTypeService payTypeService;

    @Operation(summary = "查询所有的支付方式下的所有支付通道")
    @RequestMapping("/query")
    public List<PayTypeResponseVO> GetallPayTypeResponseVOS() {
        return payTypeService.GetallPayTypeResponseVOS();
    }
}