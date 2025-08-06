package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.common.util.BeanCoper;
import com.common.util.StringUtils;
import com.main.Entity.MemberRechargeflow;
import com.main.Entity.VpnMember;
import com.main.Entity.VpnOrderinfo;
import com.main.config.PubMsgTask;
import com.main.dao.VpnMemberMapper;
import com.main.manage.ReturnCard;
import com.main.manage.responseVO.IndexResponeVO;
import com.main.manage.requestbodyvo.MemberRequestParamVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.AddMemberDays;
import com.main.manage.requestbodyvo.MemberStatusVO;
import com.main.manage.responseVO.VpnMemberVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class VpnMemberService extends ServiceImpl<VpnMemberMapper, VpnMember> implements IService<VpnMember> {

    @Autowired
    MemberRechargeflowService memberRechargeflowService;
    @Autowired
    VpnLinestatusService vpnLinestatusService;
    @Autowired
    PubMsgTask task;

    public IndexResponeVO indexQuery() {

        try {
            IndexResponeVO vo = new IndexResponeVO();
            //平台总用户数量
            LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class);
            vo.setPlatformUsers(this.count(wrapper));
            //今日注册用户数量
            LocalDateTime localDateTime = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime localDateTime2 = LocalDateTime.now().with(LocalTime.MAX);
            LambdaQueryWrapper<VpnMember> wrapper2 = Wrappers.lambdaQuery(VpnMember.class)
                    .between(VpnMember::getCreateTime, localDateTime, localDateTime2);
            vo.setTodayRegisterUsers(this.count(wrapper2));
            return vo;
        } catch (Exception e) {
            throw new BizException(1, e.getMessage());
        }
    }
    public PageResultResponseVO<List<VpnMemberVO>> MemberQuery(@RequestBody MemberRequestParamVO request) {
        PageResultResponseVO<List<VpnMemberVO>> response = new PageResultResponseVO();
        LocalDateTime begintime = (request.getCreateTimestart() == null) ? LocalDateTime.now().plusYears(-100) : request.getCreateTimeend();
        LocalDateTime endtime = (request.getCreateTimestart() == null) ? LocalDateTime.now().plusYears(100) : request.getCreateTimeend();
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .like(StringUtils.isNotBlank(request.getMemName()), VpnMember::getMemName, request.getMemName())
                .between(VpnMember::getCreateTime, begintime, endtime)
                .eq(request.getIsMember() > -1, VpnMember::getIsMember, request.getIsMember())
                .eq(StringUtils.isNotBlank(request.getInventCode()), VpnMember::getICode, request.getInventCode())
                .eq(request.getMemStatus() > -1, VpnMember::getMemStatus, request.getMemStatus())
                .eq(StringUtils.isNotBlank(request.getFatherIcode()), VpnMember::getFatherIcode, request.getFatherIcode());
        Page pagination = new Page(request.getPageIndex(), request.getPageSize());
        Page<VpnMember> page = super.page(pagination, wrapper);
        List<VpnMember> list = page.getRecords();
        List<VpnMemberVO> listvo = BeanCoper.copyList(VpnMemberVO.class, list);
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        for (int i = 0; i < listvo.size(); i++) {

            if(listvo.get(i).getExpirTime().isAfter(LocalDateTime.now())){

                listvo.get(i).setIsMember(0);
            }
        }
        response.setData(listvo);
        vpnLinestatusService.getAllUserStatus(listvo);
        return response;
    }

    public Integer EditMemberStatus(long memid, int status) {
        LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                .set(VpnMember::getMemStatus, status)
                .eq(VpnMember::getId, memid);
        boolean isSuccess = this.update(updateWrap);
        if (false == isSuccess) {
            throw new BizException(1, "编辑会员状态失败");
        } else {
            MemberStatusVO vo = new MemberStatusVO();
            vo.setMemId(memid);
            vo.setMemStatus(status);
            String vos = "memid," + vo.getMemId() + ",memstatus," + vo.getMemStatus();
            task.PubMemberStatus(vos);
            return 0;
        }
    }

    public Integer EditMemberisMember(long memid, int status) {
        LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                .set(VpnMember::getIsMember, status)
                .eq(VpnMember::getId, memid);
        boolean isSuccess = this.update(updateWrap);
        if (false == isSuccess) {
            throw new BizException(1, "编辑会员会员状态失败");
        } else {
            return 0;
        }
    }

    public Integer EditMemberExpireTime(long memid, LocalDateTime olddatetime, LocalDateTime newdateTime) {
        LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                .set(VpnMember::getExpirTime, newdateTime)
                .eq(VpnMember::getId, memid);
        boolean isSuccess = this.update(updateWrap);
        if (false == isSuccess) {
            throw new BizException(1, "编辑会员过期时间失败");
        }
        long days = ChronoUnit.DAYS.between(olddatetime, newdateTime);
        //后台修改时间后,要记录
        LambdaQueryWrapper<VpnMember> queryWrap = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getId, memid)
                .select(VpnMember::getId, VpnMember::getIsMember);
        List<VpnMember> lst = this.list(queryWrap);
        if (lst.size() <= 0) {
            throw new BizException(1, "查询会员失败");

        }
        VpnMember currentMember = lst.get(0);
        MemberRechargeflow flow = new MemberRechargeflow();
        flow.setCreateTime(LocalDateTime.now());
        flow.setOpId(2);
        flow.setOp("管理员续期");
        flow.setIsmember(currentMember.getIsMember());
        flow.setLastExpiretime(olddatetime);
        flow.setDays(days);
        flow.setNewExpiretime(newdateTime);
        flow.setRemark("");
        flow.setHashDeal("");//没有哈希交易
        flow.setMemId(memid);
        flow.setOrderId(ReturnCard.ReturnDatetimeAndGuid());
        memberRechargeflowService.AddMemberRechargeflow(flow);
        return 0;
    }

    public PageResultResponseVO<List<MemberRechargeflow>> GetAllFlows(long memid, long pageIndex, long pageSize) {

        return memberRechargeflowService.GetAllFlow(memid, pageIndex, pageSize);
    }

    public Integer EditMemberExpireDays(AddMemberDays vo) {
        LambdaQueryWrapper<VpnMember> queryWrap = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getId, vo.getMemid());
        List<VpnMember> lst = this.list(queryWrap);
        if (lst == null) {
            throw new BizException(1, "续期失败");

        } else if (lst.size() == 0) {
            throw new BizException(1, "续期失败");
        }
        VpnMember member = lst.get(0);
        if (vo.getDays() < 0) {
            if (member.getExpirTime().isBefore(LocalDateTime.now())) {
                throw new BizException(1, "会员已经过期");
            }
        }
        LocalDateTime nowdate = member.getExpirTime().isBefore(LocalDateTime.now())
                ? LocalDateTime.now().plusDays(vo.getDays())
                : member.getExpirTime().plusDays(vo.getDays());
        if (nowdate.isBefore(LocalDateTime.now())) {
            nowdate = LocalDateTime.now();
        }
        LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                .set(VpnMember::getExpirTime, nowdate)
                .eq(VpnMember::getId, vo.getMemid());
        boolean isSuccess = this.update(updateWrap);
        if (false == isSuccess) {
            throw new BizException(1, "续期失败");
        }
        MemberRechargeflow flow = new MemberRechargeflow();
        flow.setCreateTime(LocalDateTime.now());
        flow.setOpId(2);
        flow.setOp("管理员续期");
        flow.setIsmember(member.getIsMember());
        flow.setLastExpiretime(member.getExpirTime());
        flow.setDays(vo.getDays());
        flow.setNewExpiretime(nowdate);
        flow.setRemark("");
        flow.setHashDeal("");//没有哈希交易
        flow.setMemId(vo.getMemid());
        flow.setMemName(member.getMemName());
        flow.setOrderId(ReturnCard.ReturnDatetimeAndGuid());
        memberRechargeflowService.AddMemberRechargeflow(flow);
        return 0;
    }
}


