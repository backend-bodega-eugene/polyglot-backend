package com.app.service;

import cn.hutool.log.Log;
import com.alibaba.fastjson.JSONObject;
import com.app.Entity.MemberRechargeflow;
import com.app.controller.dto.ChangePasswordRequestVO;
import com.app.controller.dto.MemberChangePasswordParam;
import com.app.controller.dto.MemberRequestParam;
import com.app.controller.dto.MemberRequestParamv2;
import com.app.controller.dto.responsedto.BindUserInfoVO;
import com.app.controller.dto.responsedto.MemeberInfoStatusVO;
import com.app.controller.dto.responsedto.VpnMemberResponseVO;
import com.app.controller.dto.responsedto.VpnMemberResponseVOextend;
import com.app.dao.VpnMemberMapper;
import com.app.manage.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.Entity.VpnMember;
import com.common.exception.BizException;
import com.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author langy
 */
@Slf4j
@Service
public class AppMemberService extends ServiceImpl<VpnMemberMapper, VpnMember> {

    @Autowired
    MemberRechargeflowService memberRechargeflowService;

    @Autowired
    SystemConfigService systemConfigService;

    public VpnMemberResponseVO MemberLogin(String memberName, String memberPassword) {
        //  Result<VpnMemberResponseVO> result = new Result<>();
        if (!StringUtils.isNotBlank(memberName) || !StringUtils.isNotBlank(memberPassword)) {
            throw new BizException(1, "用户名或者密码不能为空");

        }
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(StringUtils.isNotBlank(memberName), VpnMember::getMemName, memberName)
                .eq(StringUtils.isNotBlank(memberPassword), VpnMember::getMemPassword, encodepassword(memberPassword));
        List<VpnMember> vpnMembers = this.list(wrapper);

        if (vpnMembers.size() > 0) {
            VpnMember member = vpnMembers.get(0);
            if (member.getMemStatus() == 1) {
                throw new BizException(1, "用户名已经被冻结");
            }

            VpnMemberResponseVO vo = new VpnMemberResponseVO();
            vo.setMemName(ReturnCard.GetSingleton().verifyStr(member.getMemName()));
            vo.setICode(member.getICode());
            vo.setIMemid(null == member.getIMemid() ? "" : member.getIMemid().toString());
            vo.setIMemname(member.getIMemname());
            vo.setIsMember(member.getIsMember());
            vo.setExpirTime(member.getExpirTime());
            vo.setAllowClient(member.getAllowClient());
            vo.setTrcAddress(member.getTrcAddress());
            vo.setId(member.getId());
            vo.setIsVisiter(0);
            vo.setMemStatus(member.getMemStatus());
            vo.setUid(member.getUid());
            vo.setRegistIpaddress(member.getRegistIpaddress());
            vo.setFatherIcode(member.getFatherIcode());
            //重写秘钥持有逻辑
            LocalDateTime local = LocalDateTime.now().plusDays(60);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String userSession = vo.getId() + "_" + local.format(formatter);
            vo.setLoginKey(ReturnCard.GetSingleton().EncodeSession(userSession));
            addSession(vo.getId());
            MemeberInfoStatusVO votemp = new MemeberInfoStatusVO();
            votemp.setMemId(member.getId());
            votemp.setMemStatus(member.getMemStatus());
            MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(votemp);
            //在这里要更改会员的最后登录时间
            LambdaUpdateWrapper<VpnMember> vpnMembersUpdate = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getLastLogintime, LocalDateTime.now())
                    .eq(VpnMember::getId, member.getId());
            this.update(vpnMembersUpdate);
            return vo;
        } else {
            throw new BizException(1, "用户名或者密码不正确");
        }
    }

    public VpnMemberResponseVO MemberLogin(String memberName, String memberPassword, String deviceId) {
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(StringUtils.isNotBlank(memberName), VpnMember::getMemName, memberName)
                .eq(StringUtils.isNotBlank(memberPassword), VpnMember::getMemPassword, encodepassword(memberPassword));
        List<VpnMember> vpnMembers = this.list(wrapper);

        if (vpnMembers.size() > 0) {
            VpnMember member = vpnMembers.get(0);
            if (member.getMemStatus() == 1) {
                throw new BizException(1, "用户名已经被冻结");
            }
            //判断设备数
            Integer numberClient = Integer.getInteger(systemConfigService.QueryNumberOfClients().getSystemValue());
            //用户先用设备数
            String[] devices = member.getDeviceId().split(",");
            //判断已经登录几个
            if (devices.length >= numberClient) {
                boolean istrue = false;
                for (String deivce : devices) {
                    if (deivce.equals(deviceId)) {
                        istrue = true;

                    }

                }
                if (!istrue) {
                    throw new BizException(1, "只允许登录" + numberClient + "个客户端");
                }
            }

            VpnMemberResponseVO vo = new VpnMemberResponseVO();
            vo.setMemName(ReturnCard.GetSingleton().verifyStr(member.getMemName()));
            vo.setICode(member.getICode());
            vo.setIMemid(null == member.getIMemid() ? "" : member.getIMemid().toString());
            vo.setIMemname(member.getIMemname());
            vo.setIsMember(member.getIsMember());
            vo.setExpirTime(member.getExpirTime());
            vo.setAllowClient(member.getAllowClient());
            vo.setTrcAddress(member.getTrcAddress());
            vo.setId(member.getId());
            vo.setIsVisiter(0);
            vo.setMemStatus(member.getMemStatus());
            vo.setUid(member.getUid());
            vo.setRegistIpaddress(member.getRegistIpaddress());
            vo.setFatherIcode(member.getFatherIcode());
            //重写秘钥持有逻辑
            LocalDateTime local = LocalDateTime.now().plusDays(60);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String userSession = vo.getId() + "_" + local.format(formatter);
            vo.setLoginKey(ReturnCard.GetSingleton().EncodeSession(userSession));
            addSession(vo.getId());
            MemeberInfoStatusVO votemp = new MemeberInfoStatusVO();
            votemp.setMemId(member.getId());
            votemp.setMemStatus(member.getMemStatus());
            MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(votemp);
            //在这里要更改会员的最后登录时间
            LambdaUpdateWrapper<VpnMember> vpnMembersUpdate = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getLastLogintime, LocalDateTime.now())
                    .set(VpnMember::getDeviceId, member.getDeviceId() + deviceId + ",")
                    .eq(VpnMember::getId, member.getId());
            this.update(vpnMembersUpdate);
            return vo;
        } else {
            throw new BizException(1, "用户名或者密码不正确");
        }
    }

    public synchronized VpnMemberResponseVO VisiterEnter(String deviceid, String ip) {
        //默认有效期是三天
        //检查游客是否存在,存在使用现有的,不存在创建新的,如果deviceid已经存在,就不新增了
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getDeviceId, deviceid);
        List<VpnMember> lst = this.list(wrapper);
        if (lst.size() > 0) {
            VpnMember vpnMember = lst.get(0);
            if (vpnMember.getMemStatus() == 1) {
                throw new BizException(256, "用户名已经被冻结");
            }
            if (vpnMember.getMemType() == 0) {
                throw new BizException(0, "用户已经绑定账户,请登录!");
            }
            VpnMemberResponseVO vo = new VpnMemberResponseVO();
            vo.setMemName(ReturnCard.GetSingleton().verifyStr(vpnMember.getMemName()));
            vo.setICode(vpnMember.getICode());
            vo.setIMemid(null == vpnMember.getIMemid() ? "" : vpnMember.getIMemid().toString());
            vo.setIMemname(vpnMember.getIMemname());
            vo.setIsMember(vpnMember.getIsMember());
            vo.setExpirTime(vpnMember.getExpirTime());
            vo.setAllowClient(vpnMember.getAllowClient());
            vo.setTrcAddress(vpnMember.getTrcAddress());
            vo.setId(vpnMember.getId());
            vo.setMemStatus(vpnMember.getMemStatus());
            vo.setIsVisiter(1);
            vo.setUid(vpnMember.getUid());
            vo.setRegistIpaddress(vpnMember.getRegistIpaddress());
            vo.setFatherIcode(vpnMember.getFatherIcode());
            LocalDateTime local = LocalDateTime.now().plusDays(3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String userSession = vo.getId() + "_" + local.format(formatter);
            vo.setLoginKey(ReturnCard.GetSingleton().EncodeSession(userSession));
            addSession(vo.getId());
            MemeberInfoStatusVO votemp = new MemeberInfoStatusVO();
            votemp.setMemId(vpnMember.getId());
            votemp.setMemStatus(vpnMember.getMemStatus());
            MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(votemp);
            //在这里要更改会员的最后登录时间,游客自动登录
            LambdaUpdateWrapper<VpnMember> vpnMembersUpdate = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getLastLogintime, LocalDateTime.now())
                    .eq(VpnMember::getId, vpnMember.getId());
            this.update(vpnMembersUpdate);

            return vo;
        } else {
            String newCard = GetRandom();
            VpnMember vpnMember = new VpnMember();
            VpnMemberResponseVO vo = new VpnMemberResponseVO();
            String membername = "游客" + ReturnCard.GetSingleton().GetReturnRodom(4);
            vpnMember.setMemName(membername);
            vpnMember.setEmailAddress(membername);
            vpnMember.setPhoneNumber(membername);
            vpnMember.setMemPassword(encodepassword("12341"));
            vpnMember.setICode(newCard);
            vpnMember.setIMemid(null);//这里要根据填写的邀请码,填写邀请人id,也是会员
            vpnMember.setIMemname("");//这里要根据填写的邀请码,填写邀请人账号,也是会员
            vpnMember.setMemStatus(0);
            vpnMember.setIsMember(1);
            Long days=GetSystemDays();
            vpnMember.setExpirTime(LocalDateTime.now().plusDays(days)); //这里要设置超时时间,系统默认试用赠送时长
            vpnMember.setAllowClient(1);        //这里可能将来默认也要变
            vpnMember.setCreateTime(LocalDateTime.now());
            vpnMember.setLastLogintime(LocalDateTime.now());
            vpnMember.setRemark("");
            vpnMember.setMemType(1);
            vpnMember.setDeviceId(deviceid);
            vpnMember.setUid(GetRandomUid());
            vpnMember.setRegistIpaddress(ip);
            vpnMember.setFatherIcode("");
            boolean isSuccess = this.save(vpnMember);

            if (false == isSuccess) {
                throw new BizException(1, "注册失败");
            } else {
                vo.setMemName(ReturnCard.GetSingleton().verifyStr(vpnMember.getMemName()));
                vo.setICode(vpnMember.getICode());
                vo.setIMemid(null == vpnMember.getIMemid() ? "" : vpnMember.getIMemid().toString());
                vo.setIMemname(vpnMember.getIMemname());
                vo.setIsMember(vpnMember.getIsMember());
                vo.setExpirTime(vpnMember.getExpirTime());
                vo.setAllowClient(vpnMember.getAllowClient());
                vo.setTrcAddress(vpnMember.getTrcAddress());
                vo.setId(vpnMember.getId());
                vo.setMemStatus(vpnMember.getMemStatus());
                vo.setIsVisiter(1);
                vo.setUid(vpnMember.getUid());
                vo.setRegistIpaddress(vpnMember.getRegistIpaddress());
                vo.setFatherIcode(vpnMember.getFatherIcode());
                //在这里要更改会员的最后登录时间,游客自动登录
                LocalDateTime local = LocalDateTime.now().plusDays(3);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String userSession = vo.getId() + "_" + local.format(formatter);
                vo.setLoginKey(ReturnCard.GetSingleton().EncodeSession(userSession));
                addSession(vo.getId());
                MemeberInfoStatusVO votemp = new MemeberInfoStatusVO();
                votemp.setMemId(vpnMember.getId());
                votemp.setMemStatus(vpnMember.getMemStatus());
                MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(votemp);
                LambdaUpdateWrapper<VpnMember> vpnMembersUpdate = Wrappers.lambdaUpdate(VpnMember.class)
                        .set(VpnMember::getLastLogintime, LocalDateTime.now())
                        .eq(VpnMember::getId, vpnMember.getId());
                this.update(vpnMembersUpdate);
                MemberRechargeflow flow = new MemberRechargeflow();
                flow.setCreateTime(LocalDateTime.now());
                flow.setOp("游客进入赠送");
                flow.setOpId(4);
                flow.setIsmember(vpnMember.getIsMember());
                flow.setLastExpiretime(LocalDateTime.now());
                flow.setDays(GetSystemDays());
                flow.setNewExpiretime(vpnMember.getExpirTime());
                flow.setRemark("");
                flow.setHashDeal("");//没有哈希交易
                flow.setMemId(vpnMember.getId());
                flow.setMemName(vpnMember.getMemName());
                flow.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
                memberRechargeflowService.AddMemberRechargeflow(flow);
                return vo;
            }
        }
    }

    public VpnMemberResponseVOextend GetuserInfo(Long memid) {
        if (memid == null) {

            throw new BizException(1, "没有给出id");
        }
        if (memid <= 0) {

            throw new BizException(1, "id不合法");
        }
        VpnMemberResponseVOextend vo = new VpnMemberResponseVOextend();
        LambdaQueryWrapper<VpnMember> queryWrap = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getId, memid);
        List<VpnMember> lst = this.list(queryWrap);
        if (null == lst) {
            throw new BizException(1, "没有查询到用户");
        }
        if (lst.size() == 0) {
            throw new BizException(1, "没有查询到用户");
        }
        VpnMember member = lst.get(0);
        //VpnMember member = (VpnMember) LoginContext.getTicket().getVpnAdmin();
        if (member != null) {
//            VpnMember member=lst.get(0);
            vo.setMemName(member.getMemName());
            vo.setICode(member.getICode());
            vo.setIMemid(null == member.getIMemid() ? "" : member.getIMemid().toString());
            vo.setIMemname(member.getIMemname());
            vo.setIsMember(member.getIsMember());
            vo.setExpirTime(member.getExpirTime());
            vo.setAllowClient(member.getAllowClient());
            vo.setTrcAddress(member.getTrcAddress());
            vo.setId(member.getId());
            vo.setIsVisiter(member.getMemType());
            vo.setMemStatus(member.getMemStatus());
            vo.setUid(member.getUid());

        }
        return vo;
    }

    public VpnMemberResponseVO BindUserInfo(BindUserInfoVO request) {
        VpnMemberResponseVO vo = new VpnMemberResponseVO();
        LambdaQueryWrapper<VpnMember> wrapper1 = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getId, request.getMemId());
        List<VpnMember> lst = this.list(wrapper1);
        VpnMember member = null;
        if (lst.size() > 0) {
            member = lst.get(0);
            if (member.getMemType() == 0) {
                throw new BizException(128, "用户已经绑定账户,请登录!");
            }
        }
        if (!StringUtils.isNotBlank(request.getAccountName())) {
            throw new BizException(1, "账号不能为空");
        }
        if (!StringUtils.isNotBlank(request.getPassWord())) {
            throw new BizException(1, "密码不能为空");
        }
        if (request.getPassWord().length() > 16 && request.getPassWord().length() < 8) {
            throw new BizException(1, "密码必须大于8或者小于16");
        }
        if (false == (ReturnCard.GetSingleton().IsMobile(request.getAccountName()) || StringUtils.isEmail(request.getAccountName()))) {
            throw new BizException(1, "请输入正确的手机号或者邮件地址");
        }
        if (!request.getVcode().equals("12341")) {
            throw new BizException(1, "验证码不正确");
        }

        LambdaUpdateWrapper<VpnMember> wrapper = Wrappers.lambdaUpdate(VpnMember.class)
                .set(StringUtils.isNotBlank(request.getAccountName()), VpnMember::getMemName, request.getAccountName())
                .set(StringUtils.isNotBlank(request.getAccountName()), VpnMember::getPhoneNumber, request.getAccountName())
                .set(StringUtils.isNotBlank(request.getAccountName()), VpnMember::getEmailAddress, request.getAccountName())
                .set(StringUtils.isNotBlank(request.getPassWord()), VpnMember::getMemPassword, encodepassword(request.getPassWord()))
                .set(VpnMember::getMemType, 0)
                .eq(VpnMember::getId, request.getMemId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "绑定失败");
        } else {
            vo.setMemName(ReturnCard.GetSingleton().verifyStr(request.getAccountName()));
            vo.setICode(member.getICode());
            vo.setIMemid(null == member.getIMemid() ? "" : member.getIMemid().toString());
            vo.setIMemname(member.getIMemname());
            vo.setIsMember(member.getIsMember());
            vo.setExpirTime(member.getExpirTime());
            vo.setAllowClient(member.getAllowClient());
            vo.setTrcAddress(member.getTrcAddress());
            vo.setId(member.getId());
            vo.setIsVisiter(0);
            vo.setMemStatus(member.getMemStatus());
            vo.setUid(member.getUid());
            vo.setRegistIpaddress(member.getRegistIpaddress());
            vo.setFatherIcode(member.getFatherIcode());
            //查询绑定赠送的天数
            long days = Long.parseLong(systemConfigService.GetSystemConfigBIndMemberRewardDays().getSystemValue());
            LocalDateTime bindDate = member.getExpirTime().isBefore(LocalDateTime.now())
                    ? LocalDateTime.now().plusDays(days)
                    : member.getExpirTime().plusDays(days);
            LambdaUpdateWrapper updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getExpirTime, bindDate)
                    .eq(VpnMember::getId, request.getMemId());
            this.update(updateWrap);
            MemberRechargeflow flow = new MemberRechargeflow();
            flow.setCreateTime(LocalDateTime.now());
            flow.setOpId(6);
            flow.setOp("绑定赠送");
            flow.setIsmember(member.getIsMember());
            flow.setLastExpiretime(member.getExpirTime());
            flow.setDays(days);
            flow.setNewExpiretime(bindDate);
            flow.setRemark("");
            flow.setHashDeal("");//没有哈希交易
            flow.setMemId(member.getId());
            flow.setMemName(member.getMemName());
            flow.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow);
            return vo;
        }
    }

    public VpnMemberResponseVO MemberRegister(MemberRequestParam param, String ipaddress) {

        if (!StringUtils.isNotBlank(param.getMemberAccount())) {
            throw new BizException(1, "用户名或者密码不能为空");

        }
        if (!StringUtils.isNotBlank(param.getMemberPassword())) {
            throw new BizException(1, "用户名或者密码不能为空");

        }
        if (param.getMemberPassword().length() > 16 && param.getMemberPassword().length() < 8) {
            throw new BizException(1, "密码必须大于8或者小于16");
        }
        if (false == (ReturnCard.GetSingleton().IsMobile(param.getMemberAccount()) || StringUtils.isEmail(param.getMemberAccount()))) {
            throw new BizException(1, "请输入正确的手机号或者邮件地址");
        }
        VpnMember imeMember = null;
        if (StringUtils.isNotBlank(param.getICode())) {
            LambdaQueryWrapper<VpnMember> queryWrapper = Wrappers.lambdaQuery(VpnMember.class)
                    .eq(VpnMember::getICode, param.getICode());
            var member = this.list(queryWrapper);
            if (member != null) {
                if (member.size() > 0) {
                    imeMember = member.get(0);
                    if (imeMember.getMemStatus() == 1) {
                        throw new BizException(1, "邀请码账户被冻结,不能使用");
                    }

                } else {
                    throw new BizException(1, "邀请码错误!");
                }
            } else {

                throw new BizException(1, "邀请码错误!");
            }
        }
        String newCard = GetRandom();
        VpnMember vpnMember = new VpnMember();
        vpnMember.setMemName(param.getMemberAccount());
        vpnMember.setEmailAddress(param.getMemberAccount());
        vpnMember.setPhoneNumber(param.getMemberAccount());
        vpnMember.setMemPassword(encodepassword(param.getMemberPassword()));
        vpnMember.setICode(newCard);
        vpnMember.setIMemid(imeMember == null ? null : imeMember.getId());//这里要根据填写的邀请码,填写邀请人id,也是会员
        vpnMember.setIMemname(imeMember == null ? "" : imeMember.getMemName());//这里要根据填写的邀请码,填写邀请人账号,也是会员
        vpnMember.setMemStatus(0);
        vpnMember.setIsMember(1);
//        Long memberdaysLong = 1L;
//        if (null != imeMember) {
//            memberdaysLong = new Long(systemConfigService.GetSystemConfigBIndMemberRewardDays().getSystemValue());//被推荐人绑定时赠送天数
//        }
        Long systemdays = GetSystemDays();//系统赠送的天数
       // Long inventDays = memberdaysLong + systemdays;//被邀请赠送的天数
        vpnMember.setExpirTime(LocalDateTime.now().plusDays(systemdays)); //这里要设置超时时间,系统默认试用赠送时长
        vpnMember.setAllowClient(3);        //这里可能将来默认也要变
        vpnMember.setCreateTime(LocalDateTime.now());
        vpnMember.setLastLogintime(LocalDateTime.now());
        vpnMember.setRemark(param.getProtectPassword());
        vpnMember.setMemType(0);
        vpnMember.setUid(GetRandomUid());
        vpnMember.setRegistIpaddress(ipaddress);
        vpnMember.setFatherIcode(imeMember == null ? "" : imeMember.getICode());
        boolean isSuccess = this.save(vpnMember);
        VpnMemberResponseVO vo = new VpnMemberResponseVO();
        if (false == isSuccess) {
            throw new BizException(1, "注册失败");
        } else {
            ///注册的时候被邀请的系统默认天数奖励
            MemberRechargeflow flow3 = new MemberRechargeflow();
            flow3.setCreateTime(LocalDateTime.now());
            flow3.setOpId(5);
            flow3.setOp("注册赠送");
            flow3.setIsmember(vpnMember.getIsMember());
            flow3.setLastExpiretime(LocalDateTime.now());
            flow3.setDays(systemdays);
            flow3.setNewExpiretime(LocalDateTime.now().plusDays(systemdays));
            flow3.setRemark("");
            flow3.setHashDeal("");
            flow3.setMemId(vpnMember.getId());
            flow3.setMemName(vpnMember.getMemName());
            flow3.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow3);
            vo.setMemName(ReturnCard.GetSingleton().verifyStr(vpnMember.getMemName()));
            vo.setICode(vpnMember.getICode());
            vo.setIMemid(null == vpnMember.getIMemid() ? "" : vpnMember.getIMemid().toString());
            vo.setIMemname(vpnMember.getIMemname());
            vo.setIsMember(vpnMember.getIsMember());
            vo.setExpirTime(vpnMember.getExpirTime());
            vo.setAllowClient(vpnMember.getAllowClient());
            vo.setTrcAddress(vpnMember.getTrcAddress());
            vo.setId(vpnMember.getId());
            vo.setIsVisiter(0);
            vo.setMemStatus(vpnMember.getMemStatus());
            vo.setUid(vpnMember.getUid());
            vo.setRegistIpaddress(vpnMember.getRegistIpaddress());
            vo.setFatherIcode(vpnMember.getFatherIcode());
            //在这里要更改会员的最后登录时间,注册自动登录
            LocalDateTime local = LocalDateTime.now().plusDays(3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String userSession = vo.getId() + "_" + local.format(formatter);
            vo.setLoginKey(ReturnCard.GetSingleton().EncodeSession(userSession));
            addSession(vo.getId());
            MemeberInfoStatusVO votemp = new MemeberInfoStatusVO();
            votemp.setMemId(vpnMember.getId());
            votemp.setMemStatus(vpnMember.getMemStatus());
            MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(votemp);
            LambdaUpdateWrapper<VpnMember> vpnMembersUpdate = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getLastLogintime, LocalDateTime.now())
                    .eq(VpnMember::getId, vpnMember.getId());
            this.update(vpnMembersUpdate);
            if (null != imeMember) {
                //注册时候,邀请人的奖励,注册的奖励
                LocalDateTime memberdays2 = imeMember.getExpirTime().isBefore(LocalDateTime.now())
                        ? LocalDateTime.now()
                        : imeMember.getExpirTime();
                Long memberdaysLong2 = new Long(systemConfigService.GetSystemConfigInviterRewardDays().getSystemValue());//邀请好友(推荐人绑定时)赠送的天数
                memberdays2 = memberdays2.plusDays(memberdaysLong2);
                updateRechargeflow(imeMember.getId(),memberdaysLong2,7, "绑定赠送(邀请人)");
                LambdaUpdateWrapper<VpnMember> updatemember = Wrappers.lambdaUpdate(VpnMember.class)
                        .set(VpnMember::getExpirTime,  memberdays2)
                        .eq(VpnMember::getId, imeMember.getId());
                this.update(updatemember);
                ///注册时候,邀请人的奖励,注册的奖励

//                MemberRechargeflow flow = new MemberRechargeflow();
//                flow.setCreateTime(LocalDateTime.now());
//                flow.setOpId(7);
//                flow.setOp("绑定赠送(邀请人)");
//                flow.setIsmember(imeMember.getIsMember());
//                flow.setLastExpiretime(imeMember.getExpirTime());
//                flow.setDays(memberdaysLong2);
//                flow.setNewExpiretime(imeMember.getExpirTime().plusDays(memberdaysLong2));
//                flow.setRemark("");
//                flow.setHashDeal("");//没有哈希交易
//                flow.setMemId(imeMember.getId());
//                flow.setMemName(imeMember.getMemName());
//                flow.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
//                memberRechargeflowService.AddMemberRechargeflow(flow);
                ///注册的时候被邀请人的奖励
                //被推荐人绑定时赠送天数
                Long memberdaysLong = new Long(systemConfigService.GetSystemConfigBIndMemberRewardDays().getSystemValue());
                updateRechargeflow(vpnMember.getId(),memberdaysLong,8, "绑定赠送(被邀请人)");
                LambdaUpdateWrapper<VpnMember> updatemember2 = Wrappers.lambdaUpdate(VpnMember.class)
                        .set(VpnMember::getExpirTime, vpnMember.getExpirTime().plusDays(memberdaysLong))
                        .eq(VpnMember::getId, vpnMember.getId());
                this.update(updatemember2);

//                MemberRechargeflow flow2 = new MemberRechargeflow();
//                flow2.setCreateTime(LocalDateTime.now());
//                flow2.setOpId(8);
//                flow2.setOp("绑定赠送(被邀请人)");
//                flow2.setIsmember(vpnMember.getIsMember());
//                flow2.setLastExpiretime(vpnMember.getExpirTime());
//                flow2.setDays(memberdaysLong);
//                flow2.setNewExpiretime(vpnMember.getExpirTime().plusDays(memberdaysLong));
//                flow2.setRemark("");
//                flow2.setHashDeal("");//没有哈希交易
//                flow2.setMemId(vpnMember.getId());
//                flow2.setMemName(vpnMember.getMemName());
//                flow2.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
//                memberRechargeflowService.AddMemberRechargeflow(flow2);
            }


        }
        return vo;
    }

    public synchronized VpnMemberResponseVO MemberRegisterV2(MemberRequestParamv2 param, String ipaddress) {

        if (!StringUtils.isNotBlank(param.getMemberAccount())) {
            throw new BizException(1, "用户名或者密码不能为空");

        }
        if (!StringUtils.isNotBlank(param.getMemberPassword())) {
            throw new BizException(1, "用户名或者密码不能为空");

        }
        if (param.getMemberPassword().length() > 16 && param.getMemberPassword().length() < 8) {
            throw new BizException(1, "密码必须大于8或者小于16");
        }
        if (false == (ReturnCard.GetSingleton().IsMobile(param.getMemberAccount()) || StringUtils.isEmail(param.getMemberAccount()))) {
            throw new BizException(1, "请输入正确的手机号或者邮件地址");
        }
        VpnMember imeMember = null;
        if (StringUtils.isNotBlank(param.getICode())) {
            LambdaQueryWrapper<VpnMember> queryWrapper = Wrappers.lambdaQuery(VpnMember.class)
                    .eq(VpnMember::getICode, param.getICode());
            var member = this.list(queryWrapper);
            if (member != null) {
                if (member.size() > 0) {
                    imeMember = member.get(0);
                    if (imeMember.getMemStatus() == 1) {
                        throw new BizException(1, "邀请码账户被冻结,不能使用");
                    }
                } else {
                    throw new BizException(1, "邀请码错误!");
                }
            } else {
                throw new BizException(1, "邀请码错误!");
            }
        }
        VpnMember vpnMember = new VpnMember();
        vpnMember.setMemName(param.getMemberAccount());
        vpnMember.setEmailAddress(param.getMemberAccount());
        vpnMember.setPhoneNumber(param.getMemberAccount());
        vpnMember.setMemPassword(encodepassword(param.getMemberPassword()));
        vpnMember.setICode(GetRandom());
        //这里要根据填写的邀请码,填写邀请人id,也是会员
        vpnMember.setIMemid(imeMember == null ? null : imeMember.getId());
        //这里要根据填写的邀请码,填写邀请人账号,也是会员
        vpnMember.setIMemname(imeMember == null ? "" : imeMember.getMemName());
        vpnMember.setFatherIcode(imeMember == null ? "" : imeMember.getICode());
        vpnMember.setMemStatus(0);
        vpnMember.setIsMember(1);
//        Long memberdaysLong = 1L;
//        if (null != imeMember) {
//            memberdaysLong = new Long(systemConfigService.GetSystemConfigBIndMemberRewardDays().getSystemValue());//被推荐人绑定时赠送天数
//        }
        //系统赠送的天数
        Long systemdays = GetSystemDays();
        //被邀请赠送的天数
      //  Long inventDays = memberdaysLong + systemdays;
        //这里要设置超时时间,系统默认试用赠送时长
        vpnMember.setExpirTime(LocalDateTime.now().plusDays(systemdays));
        //这里可能将来默认也要变
        vpnMember.setAllowClient(3);
        vpnMember.setCreateTime(LocalDateTime.now());
        vpnMember.setLastLogintime(LocalDateTime.now());
        vpnMember.setRemark(param.getProtectPassword());
        vpnMember.setMemType(0);
        vpnMember.setUid(GetRandomUid());
        vpnMember.setRegistIpaddress(ipaddress);
        //根据设备id判断是否存在的用户
        VpnMember currentVpnmember = null;
        if (StringUtils.isNotBlank(param.getDeviceId())) {
            LambdaQueryWrapper<VpnMember> queryWrap = Wrappers.lambdaQuery(VpnMember.class)
                    .eq(VpnMember::getDeviceId, param.getDeviceId());
           var memlist= this.list(queryWrap);
            if(memlist==null){
                throw new BizException(1, "设备id错误");
            }
            if(memlist.size() == 0){
                throw new BizException(1, "设备id错误");
            }
            currentVpnmember=memlist.get(0);
            //已经转换的用户,不能重复注册
            if (currentVpnmember.getMemType() == 0) {
                throw new BizException(1, "账户已经注册,请登录");
            }
        }
        if(StringUtils.isNotBlank(param.getMemberAccount())) {
            LambdaQueryWrapper<VpnMember> queryWrap = Wrappers.lambdaQuery(VpnMember.class)
                    .eq(VpnMember::getMemName, param.getMemberAccount());
            var memlist = this.list(queryWrap);
            if(memlist != null) {
                if (memlist.size() > 0) {
                    currentVpnmember = memlist.get(0);
                    if (currentVpnmember.getMemName().equals(param.getMemberAccount())) {
                        throw new BizException(1, "账户已经注册,请登录");
                    }
                }
            }
        }
        boolean isSuccess = false;
        if (currentVpnmember == null) {
            isSuccess = this.save(vpnMember);
            ///注册的时候被邀请的系统默认天数奖励
            MemberRechargeflow flow3 = new MemberRechargeflow();
            flow3.setCreateTime(LocalDateTime.now());
            flow3.setOpId(5);
            flow3.setOp("注册赠送");
            flow3.setIsmember(vpnMember.getIsMember());
            flow3.setLastExpiretime(LocalDateTime.now());
            flow3.setDays(systemdays);
            flow3.setNewExpiretime(LocalDateTime.now().plusDays(systemdays));
            flow3.setRemark("");
            flow3.setHashDeal("");
            flow3.setMemId(vpnMember.getId());
            flow3.setMemName(vpnMember.getMemName());
            flow3.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow3);
        } else {
            //游客注册奖励
            LocalDateTime newdate=currentVpnmember.getExpirTime().isBefore(LocalDateTime.now())
                    ?LocalDateTime.now()
                    :currentVpnmember.getExpirTime();
            MemberRechargeflow flow3 = new MemberRechargeflow();
            flow3.setCreateTime(LocalDateTime.now());
            flow3.setOpId(5);
            flow3.setOp("注册赠送");
            flow3.setIsmember(currentVpnmember.getIsMember());
            flow3.setLastExpiretime(newdate);
            flow3.setDays(systemdays);
            flow3.setNewExpiretime(newdate.plusDays(systemdays));
            flow3.setRemark("");
            flow3.setHashDeal("");
            flow3.setMemId(currentVpnmember.getId());
            flow3.setMemName(currentVpnmember.getMemName());
            flow3.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow3);
            vpnMember.setId(currentVpnmember.getId());
//            vpnMember.setIMemid(currentVpnmember.getIMemid());
//            vpnMember.setIMemname(currentVpnmember.getIMemname());
//            vpnMember.setFatherIcode(currentVpnmember.getICode());
            //vpnMember.setFatherIcode(currentVpnmember.getICode());
            vpnMember.setExpirTime(newdate.plusDays(systemdays));
            vpnMember.setICode(currentVpnmember.getICode());
            vpnMember.setUid(currentVpnmember.getUid());
            isSuccess = this.updateById(vpnMember);
            //当注册用户改为正式用户后,把已经创建关系的名称改为新的名称
            updateMemberNames(vpnMember.getId(),vpnMember.getMemName());
        }
        VpnMemberResponseVO vo = new VpnMemberResponseVO();
        if (false == isSuccess) {
            throw new BizException(1, "注册失败");
        } else {
            vo.setMemName(ReturnCard.GetSingleton().verifyStr(vpnMember.getMemName()));
            vo.setICode(vpnMember.getICode());
            vo.setIMemid(null == vpnMember.getIMemid() ? "" : vpnMember.getIMemid().toString());
            vo.setIMemname(vpnMember.getIMemname());
            vo.setIsMember(vpnMember.getIsMember());
            vo.setExpirTime(vpnMember.getExpirTime());
            vo.setAllowClient(vpnMember.getAllowClient());
            vo.setTrcAddress(vpnMember.getTrcAddress());
            vo.setId(vpnMember.getId());
            vo.setIsVisiter(0);
            vo.setMemStatus(vpnMember.getMemStatus());
            vo.setUid(vpnMember.getUid());
            vo.setRegistIpaddress(vpnMember.getRegistIpaddress());
            vo.setFatherIcode(vpnMember.getFatherIcode());
            //在这里要更改会员的最后登录时间,注册自动登录
            LocalDateTime local = LocalDateTime.now().plusDays(3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String userSession = vo.getId() + "_" + local.format(formatter);
            vo.setLoginKey(ReturnCard.GetSingleton().EncodeSession(userSession));
            addSession(vo.getId());
            MemeberInfoStatusVO votemp = new MemeberInfoStatusVO();
            votemp.setMemId(vpnMember.getId());
            votemp.setMemStatus(vpnMember.getMemStatus());
            MemeberInfoStatus.getInstance().AddMemeberInfoStatusVO(votemp);
            LambdaUpdateWrapper<VpnMember> vpnMembersUpdate = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getLastLogintime, LocalDateTime.now())
                    .eq(VpnMember::getId, vpnMember.getId());
            this.update(vpnMembersUpdate);
            if (null != imeMember) {
                //注册时候,邀请人的奖励,注册的奖励

                LocalDateTime memberdays2 = imeMember.getExpirTime().isBefore(LocalDateTime.now()) ? LocalDateTime.now() : imeMember.getExpirTime();
                Long memberdaysLong2 = new Long(systemConfigService.GetSystemConfigInviterRewardDays().getSystemValue());//邀请好友(推荐人绑定时)赠送的天数
                memberdays2 = memberdays2.plusDays(memberdaysLong2);
                ///注册时候,邀请人的奖励,注册的奖励
                updateRechargeflow(imeMember.getId(),memberdaysLong2,7, "绑定赠送(邀请人)");
                LambdaUpdateWrapper<VpnMember> updatemember = Wrappers.lambdaUpdate(VpnMember.class)
                        .set(VpnMember::getExpirTime, memberdays2)
                        .eq(VpnMember::getId, imeMember.getId());
                this.update(updatemember);

                ///注册的时候被邀请人的奖励
                Long memberdaysLong = new Long(systemConfigService.GetSystemConfigBIndMemberRewardDays().getSystemValue());
                updateRechargeflow(vpnMember.getId(),memberdaysLong,8, "绑定赠送(被邀请人)");
                LambdaUpdateWrapper<VpnMember> updatemember2 = Wrappers.lambdaUpdate(VpnMember.class)
                        .set(VpnMember::getExpirTime, vpnMember.getExpirTime().plusDays(memberdaysLong))
                        .eq(VpnMember::getId, vpnMember.getId());
                this.update(updatemember2);
            }
        }
        return vo;
    }

    public Integer MemberChangePassword(@RequestBody MemberChangePasswordParam param) {

        if (param.getMemberPassword().length() > 16 || param.getMemberPassword().length() < 8) {
            throw new BizException(1, "密码必须大于16或者小于8");
        }
        LambdaQueryWrapper<VpnMember> queryWrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getMemName, param.getMemberAccount());
        var userlist = this.list(queryWrapper);
        if (userlist == null) {
            throw new BizException(1, "会员名称不存在");
        }
        if (userlist.size() == 0) {
            throw new BizException(1, "会员名称不存在");
        }
        if (!param.getPasswordProblem().equals(userlist.get(0).getRemark())) {

            throw new BizException(1, "密保问题错误");
        }
        LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                .set(VpnMember::getMemPassword, encodepassword(param.getMemberPassword()))
                .eq(VpnMember::getMemName, param.getMemberAccount())
                .eq(VpnMember::getId, userlist.get(0).getId());
        boolean isSuccess = this.update(updateWrap);
        if (false == isSuccess) {
            throw new BizException(1, "修改密码失败");
        } else {
            return 0;
        }
    }

    public Integer MemberChangePasswordv2(@RequestBody ChangePasswordRequestVO param, Long memid) {
//        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
//                .eq(VpnMember::getMemPassword, encodepassword(param.getOldPassword()))
//                .eq(VpnMember::getId, memid);
        VpnMember member = this.getById(memid);
        String oldPassword = param.getOldPassword();
        if (!encodepassword(oldPassword).equals(member.getMemPassword())) {
            throw new BizException(1, "旧密码错误!");
        }
        if (param.getOldPassword().equals(param.getNewPassword())) {
            throw new BizException(1, "新密码不能和旧密码一样!");
        }
        if (param.getNewPassword().length() < 8 || param.getNewPassword().length() > 16) {

            throw new BizException(1, "密码长度为8到16位,并且只能是数字和字母!");
        }
        if (!isLetterDigit(param.getNewPassword())) {
            throw new BizException(1, "密码只能是数字和字母!");

        }
        LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                .set(VpnMember::getMemPassword, encodepassword(param.getNewPassword()))
                .eq(VpnMember::getMemPassword, encodepassword(param.getOldPassword()))
                .eq(VpnMember::getId, memid);
        boolean isSuccess = this.update(updateWrap);
        if (false == isSuccess) {
            throw new BizException(1, "修改密码失败");
        } else {
            return 0;
        }
    }

    public Integer scanQrbyicode(long memid, String iCode) {
        if (!StringUtils.isNotBlank(iCode) || memid <= 0) {
            throw new BizException(1, "请填写有效的邀请码或者会员id非法");
        }
        //根据邀请码,拿到邀请人 ,然后把icode的memid赋值给memid的IMemid
        LambdaQueryWrapper<VpnMember> memberwrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getICode, iCode);
        List<VpnMember> lst = this.list(memberwrapper);
        if (lst.size() > 0) {
            VpnMember member = lst.get(0);
            if (member.getMemStatus() == 1) {
                throw new BizException(1, "邀请码账户被冻结,不能绑定");
            }
            //不能绑定自己
            VpnMember member2 = this.getById(memid);
            if (null == member2) {
                throw new BizException(1, "会员id异常");
            }
            if (null != member2.getIMemid()) {
                //不能更改绑定
                throw new BizException(1, "已经绑定邀请码,不能更改绑定");
            }
            if (member.getId() == memid) {
                throw new BizException(1, "不能绑定自己");
            }

            //增加被推荐人的天数
            //更改绑定,并且被推荐人赠送天数,绑定关系
            LocalDateTime memberdays = member2.getExpirTime().isBefore(LocalDateTime.now())
                    ? LocalDateTime.now()
                    : member2.getExpirTime();
            Long memberdaysLong = new Long(systemConfigService.GetSystemConfigBIndMemberRewardDays().getSystemValue());//被推荐人绑定时赠送天数
            memberdaysLong = memberdaysLong <= 0 ? 1 : memberdaysLong;
            memberdays = memberdays.plusDays(memberdaysLong);
            LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getIMemid, member.getId())
                    .set(VpnMember::getIMemname, member.getMemName())
                    .set(VpnMember::getFatherIcode, member.getICode())
                    .set(VpnMember::getExpirTime, memberdays)
                    .eq(VpnMember::getId, memid);
            this.update(updateWrap);

            //被推荐人的记录
            MemberRechargeflow flow = new MemberRechargeflow();
            flow.setCreateTime(LocalDateTime.now());
            flow.setOpId(9);
            flow.setOp("扫码推荐赠送(被推荐人)");
            flow.setIsmember(member2.getIsMember());
            flow.setLastExpiretime(member2.getExpirTime());
            flow.setDays(memberdaysLong);
            flow.setNewExpiretime(memberdays);
            flow.setRemark("");
            //没有哈希交易
            flow.setHashDeal("");
            flow.setMemId(member2.getId());
            flow.setMemName(member2.getMemName());
            flow.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow);
            //推荐人的记录
            //增加推荐人的天数
            LocalDateTime memberdays2 = member.getExpirTime().isBefore(LocalDateTime.now())
                    ? LocalDateTime.now()
                    : member.getExpirTime();
            //邀请好友(推荐人绑定时)赠送的天数
            Long memberdaysLong2 = new Long(systemConfigService.GetSystemConfigInviterRewardDays().getSystemValue());
            memberdays2 = memberdays2.plusDays(memberdaysLong2);
            LambdaUpdateWrapper<VpnMember> updatemember = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getExpirTime, memberdays2)
                    .eq(VpnMember::getId, member.getId());
            this.update(updatemember);
            memberdaysLong2 = memberdaysLong2 <= 0 ? 1 : memberdaysLong2;
            MemberRechargeflow flow2 = new MemberRechargeflow();
            flow2.setCreateTime(LocalDateTime.now());
            flow2.setOpId(10);
            flow2.setOp("扫码推荐赠送(推荐人)");
            flow2.setIsmember(member.getIsMember());
            flow2.setLastExpiretime(member.getExpirTime());
            flow2.setDays(memberdaysLong2);
            flow2.setNewExpiretime(memberdays2);
            flow2.setRemark("");
            flow2.setHashDeal("");//没有哈希交易
            flow2.setMemId(member.getId());
            flow2.setMemName(member.getMemName());
            flow2.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow2);
        } else {
            throw new BizException(1, "请填写有效的邀请码或者会员id非法");
        }
        return 0;
    }

    public Integer scanQrbyicode(long memid, String iCode, String deviceId) {
        if (!StringUtils.isNotBlank(iCode) || memid <= 0) {
            throw new BizException(1, "请填写有效的邀请码或者会员id非法");
        }
        //根据邀请码,拿到邀请人 ,然后把icode的memid赋值给memid的IMemid
        LambdaQueryWrapper<VpnMember> memberwrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getICode, iCode);
        List<VpnMember> lst = this.list(memberwrapper);
        if (lst.size() > 0) {
            VpnMember member = lst.get(0);

            LambdaQueryWrapper<VpnMember> wrapper2 = Wrappers.lambdaQuery(VpnMember.class)
                    .eq(VpnMember::getId, memid);
            List<VpnMember> lst2 = this.list(wrapper2);
            VpnMember member2 = null;
            if (lst2.size() > 0) {
                member2 = lst2.get(0);
                if (null != member2.getIMemid() ) {
                    //不能更改绑定
                    throw new BizException(1, "已经绑定邀请码,不能更改绑定");
                }
            }
            if (StringUtils.isNotBlank(member2.getDeviceId())) {
                if (StringUtils.isNotBlank(deviceId)) {
                    if (member2.getDeviceId().equals(deviceId)) {

                        throw new BizException(1, "不能绑定同设备账号");
                    }
                }
            }
            //不能绑定自己
            if (member.getId() == memid) {
                throw new BizException(1, "不能绑定自己");
            }
            if (null == member2) {
                throw new BizException(1, "会员id异常");
            }
            //增加被推荐人的天数
            //更改绑定,并且被推荐人赠送天数,绑定关系
            LocalDateTime memberdays = member2.getExpirTime().isBefore(LocalDateTime.now()) ? LocalDateTime.now() : member2.getExpirTime();
            Long memberdaysLong = new Long(systemConfigService.GetSystemConfigInviteeBonusDays().getSystemValue());//被推荐人绑定时赠送天数
            memberdays = memberdays.plusDays(memberdaysLong);
            LambdaUpdateWrapper<VpnMember> updateWrap = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getIMemid, member.getId())
                    .set(VpnMember::getIMemname, member.getMemName())
                    .set(VpnMember::getFatherIcode, member.getICode())
                    .set(VpnMember::getExpirTime, memberdays)
                    .eq(VpnMember::getId, memid);
            this.update(updateWrap);

            //被推荐人的记录
            MemberRechargeflow flow = new MemberRechargeflow();
            flow.setCreateTime(LocalDateTime.now());
            flow.setOpId(9);
            flow.setOp("扫码推荐赠送(被推荐人)");
            flow.setIsmember(member2.getIsMember());
            flow.setLastExpiretime(member2.getExpirTime());
            flow.setDays(memberdaysLong);
            flow.setNewExpiretime(memberdays);
            flow.setRemark("");
            //没有哈希交易
            flow.setHashDeal("");
            flow.setMemId(member2.getId());
            flow.setMemName(member2.getMemName());
            flow.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow);
            //推荐人的记录
            //增加推荐人的天数
            LocalDateTime memberdays2 = member.getExpirTime().isBefore(LocalDateTime.now())
                    ? LocalDateTime.now()
                    : member.getExpirTime();
            //邀请好友(推荐人绑定时)赠送的天数
            Long memberdaysLong2 = new Long(systemConfigService.GetSystemConfigInviterRewardDays().getSystemValue());
            memberdays2 = memberdays2.plusDays(memberdaysLong2);
            LambdaUpdateWrapper<VpnMember> updatemember = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getExpirTime, memberdays2)
                    .eq(VpnMember::getId, member.getId());
            this.update(updatemember);
            MemberRechargeflow flow2 = new MemberRechargeflow();
            flow2.setCreateTime(LocalDateTime.now());
            flow2.setOpId(10);
            flow2.setOp("扫码推荐赠送(推荐人)");
            flow2.setIsmember(member.getIsMember());
            flow2.setLastExpiretime(member.getExpirTime());
            flow2.setDays(memberdaysLong2);
            flow2.setNewExpiretime(memberdays2);
            flow2.setRemark("");
            //没有哈希交易
            flow2.setHashDeal("");
            flow2.setMemId(member.getId());
            flow2.setMemName(member.getMemName());
            flow2.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
            memberRechargeflowService.AddMemberRechargeflow(flow2);
        } else {
            throw new BizException(1, "请填写有效的邀请码或者会员id非法");
        }
        return 0;
    }
    private void updateMemberNames(Long id,String memName){
        if(null==id){
            return;
        }
        if(id<=0){
            return;
        }
        if(StringUtils.isNotBlank(memName)) {
            LambdaUpdateWrapper<VpnMember> updatemember2 = Wrappers.lambdaUpdate(VpnMember.class)
                    .set(VpnMember::getIMemname, memName)
                    .eq(VpnMember::getIMemid, id);
            this.update(updatemember2);
        }

    }
    public void deleteDevice(Long memId, String deviceId) {
        VpnMember member = this.getById(memId);
        String[] devices = member.getDeviceId().split(",");
        String newDevices = "";
        for (String device : devices) {
            if (device.equals(deviceId)) {
                continue;
            }
            newDevices = device + ",";
        }
        newDevices = newDevices.substring(0, newDevices.length() - 1);
        member.setDeviceId(newDevices);
        this.updateById(member);
        //appMemberService.deleteDevice(getSession());
    }
    private void updateRechargeflow(Long memId,Long days,Integer opId,String op){
        VpnMember member=this.getById(memId);
        MemberRechargeflow flow = new MemberRechargeflow();
        flow.setCreateTime(LocalDateTime.now());
        flow.setOpId(opId);
        flow.setOp(op);
        flow.setIsmember(member.getIsMember());
        flow.setLastExpiretime(member.getExpirTime());
        flow.setDays(days);
        flow.setNewExpiretime(member.getExpirTime().plusDays(days));
        flow.setRemark("");
        //没有哈希交易
        flow.setHashDeal("");
        flow.setMemId(member.getId());
        flow.setMemName(member.getMemName());
        flow.setOrderId(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
        memberRechargeflowService.AddMemberRechargeflow(flow);
    }
    private final static String PASSWORD_KEY = "0000100000001111";

    private String encodepassword(String password) {
        return ReturnCard.GetSingleton().AESEncrypt(password, PASSWORD_KEY);
    }

    private String GetRandomUid() {
        String newCard;
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .select(VpnMember::getICode);
        List<VpnMember> vpnMembers = this.list(wrapper);
        List<String> lst = new ArrayList<>();
        for (VpnMember temp : vpnMembers) {
            lst.add(temp.getUid());
        }
        newCard = ReturnCard.GetSingleton().ReturnRodom(lst, 8);
        return newCard;
    }

    private void addSession(long memid) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        servletRequestAttributes.getRequest().getSession().setAttribute("memid", memid);
    }

    private boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }

    private long GetSystemDays() {
        //目前还没有实现系统配置,默认3天
        Long days = new Long(systemConfigService.GetSystemConfigInviteeBonusDays().getSystemValue());
        return days;
    }

    private long GetInventSystemDays() {
        //目前还没有实现系统配置,默认3天
        Long days = new Long(systemConfigService.GetSystemConfigInviterRewardDays().getSystemValue());
        return days;
    }

    private String GetRandom() {
        String newCard;
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .select(VpnMember::getICode);
        List<VpnMember> vpnMembers = this.list(wrapper);
        List<String> lst = new ArrayList<>();
        for (VpnMember temp : vpnMembers) {
            lst.add(temp.getICode());
        }
        newCard = ReturnCard.GetSingleton().ReturnRodom(lst, 8);
        return newCard;
    }

}