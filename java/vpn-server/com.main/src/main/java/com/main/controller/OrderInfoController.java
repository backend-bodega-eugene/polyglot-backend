package com.main.controller;

import com.main.Entity.PayInfo;
import com.main.Entity.VpnOrderinfo;
import com.main.manage.requestbodyvo.OderQuestParam;
import com.main.manage.requestbodyvo.OrderRemakrParamVO;
import com.main.manage.requestbodyvo.OrderStatusParamVO;
import com.main.manage.responseVO.OrderListVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.responseVO.SumAllTotalVO;
import com.main.service.VpnLineLineService;
import com.main.service.VpnOrderinfoService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Api(value = "订单管理", tags = {"订单查询,仅有未处理的订单"})
@RestController
@RequestMapping(value = "/backend/orderinfo", method = {RequestMethod.POST})
public class OrderInfoController {
    @Autowired
    VpnOrderinfoService vpnOrderinfoService;

    @Operation(summary = "订单查询")
    @RequestMapping("/query")
    public PageResultResponseVO<OrderListVO> OrderinfoQuery(@RequestBody OderQuestParam param) {
        return vpnOrderinfoService.GetAllorderInfo(
                param.getPageIndex(),
                param.getPageSize(),
                param.getOrderGuid(),
                param.getMemId(),
                param.getMemName(),
                param.getStartDatetime(),
                param.getEndDatetime(),
                param.getPayIds()
                //2023-13-07 23:59:59
        );
    }

    @Operation(summary = "确认到账")
    @RequestMapping("/confirmorder")
    public Boolean confirmOrder(@RequestBody OrderStatusParamVO vo) {
        return vpnOrderinfoService.confirmOrder(vo);
    }

    @Operation(summary = "已完成和已取消的订单统计")
    @RequestMapping("/sumAllOrder")
    public SumAllTotalVO sumAllOrder() {
        return vpnOrderinfoService.sumAllOrder();
    }

    @Operation(summary = "处理中充值金额")
    @RequestMapping("/handingallorder")
    public BigDecimal handingAllOrder() {
        return vpnOrderinfoService.handingAllOrder();
    }

    @Operation(summary = "取消订单")
    @RequestMapping("/cancelorder")
    public Boolean cancelOrder(@RequestBody OrderRemakrParamVO vo) {
        return vpnOrderinfoService.cancelOrder(vo);
    }
}