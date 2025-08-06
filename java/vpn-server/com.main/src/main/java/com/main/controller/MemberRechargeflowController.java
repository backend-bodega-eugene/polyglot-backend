package com.main.controller;

import com.main.Entity.MemberRechargeflow;
import com.main.manage.requestbodyvo.GetAllFlowsExtendVO;
import com.main.manage.responseVO.MemberRechargeflowVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.service.MemberRechargeflowService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Api(value = "奖励发送记录", tags = {"奖励发送记录"})
@RestController
@RequestMapping(value = "/backend/memberflows", method = {RequestMethod.POST})
public class MemberRechargeflowController {
    @Autowired
    MemberRechargeflowService memberRechargeflowService;

    @Operation(summary = "查询会员奖励发放记录")
    @RequestMapping("/getallflows")
    public PageResultResponseVO<MemberRechargeflowVO> GetAllFlows(@RequestBody GetAllFlowsExtendVO vo) {

        return memberRechargeflowService.GetAllFlowsByCondition(
                vo.getPageIndex(),
                vo.getPageSize(),
                vo.getStartDatetime(),
                vo.getEndDateTime(),
                vo.getOpId(),
                vo.getMemName(),
                vo.getMemId(),
                vo.getId());
    }

    @Operation(summary = "获取奖励的天数")
    @RequestMapping("/totalbound")
    public Long totalBound() {
        return memberRechargeflowService.totalBound();
    }

    @Operation(summary = "获取所有奖励类型")
    @RequestMapping("/getopList")
    public Map<Integer, String> getOpList(@RequestBody GetAllFlowsExtendVO vo) {

        Map<Integer, String> map = new HashMap<>();
        map.put(1, "开通会员");
        map.put(2, "管理员续期");
        map.put(3, "扫码推荐赠送");
        map.put(4, "游客进入赠送");
        map.put(5, "注册赠送");
        map.put(6, "绑定赠送");
        return map;
    }
}
