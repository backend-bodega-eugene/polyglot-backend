package com.main.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.main.Entity.MemberRechargeflow;
import com.main.Entity.VpnMember;
import com.main.Entity.VpnOrderinfo;
import com.main.manage.responseVO.IndexResponeVO;
import com.main.manage.requestbodyvo.MemberRequestParamVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.AddMemberDays;
import com.main.manage.requestbodyvo.EditMemberVO;
import com.main.manage.requestbodyvo.GetAllFlowsVO;
import com.main.manage.responseVO.VpnMemberVO;
import com.main.service.SystemConfigService;
import com.main.service.VpnMemberService;
import com.main.service.VpnOrderinfoService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "会员中心", tags = {"会员查询,添加,修改,更改状态"})
@RestController
@RequestMapping(value = "/backend/member", method = {RequestMethod.POST})
public class MemberInfoController {

    @Autowired
    VpnMemberService vpnMemberService;
    @Autowired
    SystemConfigService systemConfigService;
    @Autowired
    VpnOrderinfoService vpnOrderinfoService;

    @Operation(summary = "首页查询")
    @RequestMapping("/indexquery")
    public IndexResponeVO IndexQuery() {
        var vo = vpnMemberService.indexQuery();

        vo.setMemberAmounts(vpnOrderinfoService.getallMemberAmount());
        vo.setPlatformMembers(vpnOrderinfoService.getallMember());
        return vo;
    }

    @Operation(summary = "会员查询")
    @RequestMapping("/query")
    public PageResultResponseVO<List<VpnMemberVO>> MemberQuery(@RequestBody MemberRequestParamVO request) {
        return vpnMemberService.MemberQuery(request);
    }

    @Operation(summary = "编辑会员状态 需要参数:memid,status 其他参数忽略")
    @RequestMapping("/editstatus")
    public Integer EditMemberStatus(@RequestBody EditMemberVO vo) {
        return vpnMemberService.EditMemberStatus(vo.getMemid(), vo.getStatus());
    }

    @Operation(summary = "编辑会员到期时间 需要参数 memid,olddatetime,newdateTime其他参数忽略")
    @RequestMapping("/editexpiretime")
    public Integer EditMemberExpireTime(@RequestBody EditMemberVO vo) {
        return vpnMemberService.EditMemberExpireTime(vo.getMemid(), vo.getOlddatetime(), vo.getNewdateTime());
    }

    @Operation(summary = "查询会员历史  需要参数 memid其他参数忽略")
    @RequestMapping("/getallflows")
    public PageResultResponseVO<List<MemberRechargeflow>> GetAllFlows(@RequestBody GetAllFlowsVO vo) {

        return vpnMemberService.GetAllFlows(vo.getMemid(), vo.getPageIndex(), vo.getPageSize());
    }

    @Operation(summary = "修改会员身份 需要参数 memid,isMember其他参数忽略")
    @RequestMapping("/editismember")
    public Integer EditMemberisMember(@RequestBody EditMemberVO vo) {
        return vpnMemberService.EditMemberisMember(vo.getMemid(), vo.getIsMember());
    }

    @Operation(summary = "编辑会员到期时间 输入数字,负数为减少天数,正数为增加天数")
    @RequestMapping("/editmemberexpiredays")
    public Integer EditMemberExpireDays(@RequestBody AddMemberDays vo) {
        return vpnMemberService.EditMemberExpireDays(vo);
    }
}
