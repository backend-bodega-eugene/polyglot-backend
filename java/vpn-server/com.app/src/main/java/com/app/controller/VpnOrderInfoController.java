package com.app.controller;

import com.app.Entity.VpnOrderinfo;
import com.app.controller.base.BaseController;
import com.app.controller.dto.*;
import com.app.service.VpnOrderInfoService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Api(value = "订单", tags = {"用户订单查询,订单下单"})
@RestController
@RequestMapping(value = "/app/orderinfo", method = {RequestMethod.POST})
public class VpnOrderInfoController extends BaseController {
    @Autowired
    VpnOrderInfoService vpnOrderInfoService;

    @Operation(summary = "用户订单查询 按订单状态,时间查询,用户id限制")
    @RequestMapping("/query")
    public List<VpnOrderinfo> vpnorderQuery(@RequestBody UserQueryRequestParam dto) {
        return vpnOrderInfoService.VpnOrderInfoQuery(dto, getSession());
    }

    @Operation(summary = "用户下单")
    @RequestMapping("/booking")
    @Transactional
    public VpnOrderinfo vpnOrderInfoBooking(@RequestBody @Valid VpnOrderInfoBookingRequestParam param) {

        return vpnOrderInfoService.VpnOrderInfoBooking(param, getSession());
    }

    @Operation(summary = "用户下单检查")
    @RequestMapping("/checkOrder")
    public Boolean checkOrder() {

        return vpnOrderInfoService.checkOrder(getSession());
    }

    @Operation(summary = "用户取消订单")
    @RequestMapping("/cancelOrder")
    public Boolean cancelOrder(@RequestBody IdDTOVO vo) {

        return vpnOrderInfoService.cancelOrder(vo.getId(), getSession());
    }

    @Operation(summary = "用户上传支付凭证")
    @RequestMapping("/uploadimg")
    public Boolean uploadImg(@RequestBody UserUploadOrderImg vo) {
        return vpnOrderInfoService.uploadImg(vo, getSession());
    }

}