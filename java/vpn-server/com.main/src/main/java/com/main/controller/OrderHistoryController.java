package com.main.controller;

import com.main.Entity.VpnOrderinfo;
import com.main.manage.requestbodyvo.OderQuestParam;
import com.main.manage.requestbodyvo.OderQuestParamVO;
import com.main.manage.responseVO.OrderListVHistoryO;
import com.main.manage.responseVO.OrderListVO;
import com.main.manage.responseVO.PageResultResponseVO;
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
import java.util.List;

@Slf4j
@Api(value = "订单历史记录", tags = {"订单历史查询"})
@RestController
@RequestMapping(value = "/backend/orderhistory", method = {RequestMethod.POST})
public class OrderHistoryController {
    @Autowired
    VpnOrderinfoService vpnOrderinfoService;

    @Operation(summary = "订单查询查询")
    @RequestMapping("/query")
    public PageResultResponseVO<OrderListVHistoryO> OrderinfoQuery(@RequestBody OderQuestParamVO param) {
        return vpnOrderinfoService.GetAllorderInfoHistory(
                param.getPageIndex(),
                param.getPageSize(),
                param.getOrderGuid(),
                param.getMemId(),
                param.getMemName(),
                param.getOrderStatus(),
                param.getStartDatetime(),
                param.getEndDatetime(),
                param.getPayIds()
        );
    }
}