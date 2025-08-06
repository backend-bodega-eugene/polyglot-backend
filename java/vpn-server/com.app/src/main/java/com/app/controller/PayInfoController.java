package com.app.controller;

import com.app.Entity.PayInfo;
import com.app.controller.dto.IdDTOVO;
import com.app.controller.dto.PayInfoAmountVO;
import com.app.controller.dto.responseVO.PayInfoVO;
import com.app.service.PayInfoService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(value = "支付通道", tags = {"支付通道查询 修改,删除,增加"})
@RestController
@RequestMapping(value = "/app/payinfo", method = {RequestMethod.POST})
public class PayInfoController {
    @Autowired
    PayInfoService payInfoService;

    @Operation(summary = "查询所有的支付方式下的所有支付通道")
    @RequestMapping("/query")
    public List<PayInfoVO> GetallPayTypeResponseVOS() {
        return payInfoService.GetallPayTypeResponseVOS();
    }

    @Operation(summary = "查询所有的支付方式下的所有支付通道")
    @RequestMapping("/querybyamount")
    public List<PayInfoVO> GetallPayTypeResponseVOS(@RequestBody PayInfoAmountVO vo) {
        return payInfoService.GetallPayTypeResponseVOS(vo.getAmount());
    }

    @Operation(summary = "查询所有的支付方式下的所有支付通道")
    @RequestMapping("/getone")
    public PayInfo getOne(@RequestBody IdDTOVO dto) {
        return payInfoService.getOne(dto.getId());
    }
}
