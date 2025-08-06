package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.exception.BizException;
import com.main.Entity.MemberRechargeflow;
import com.main.Entity.VpnPackage;
import com.main.manage.ReturnCard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.main.Entity.VpnMember;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DealHashCallbackService {

    @Autowired
    VpnOrderinfoService vpnOrderinfoService;
    @Autowired
    VpnMemberService vpnMemberService;
    @Autowired
    VpnPackageService vpnPackageService;
    @Autowired
    MemberRechargeflowService memberRechargeflowService;

    public void DealHashCallbackHandle(String hashcode, String price) {
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getTrcAddress, hashcode);
        List<VpnMember> lst = vpnMemberService.list(wrapper);
        if (lst.size() > 0) {
            BigDecimal amount = new BigDecimal(price);
            LambdaQueryWrapper<VpnPackage> wrapper2 = Wrappers.lambdaQuery(VpnPackage.class)
                    .eq(VpnPackage::getMempackageAmount, amount);
            List<VpnPackage> lst2 = vpnPackageService.list(wrapper2);
            if (lst2.size() > 0) {
                //增加会员对应的时间
                VpnMember member = lst.get(0);
                Duration duration = Duration.between(member.getExpirTime(), LocalDateTime.now());
                int days = 0;
                if (duration.toDays() > 0) {
                    days = (int) duration.toDays() + lst2.get(0).getMempackageQuantity();
                } else {
                    days = lst2.get(0).getMempackageQuantity();

                }
                LambdaUpdateWrapper<VpnMember> wrapperUPdate = Wrappers.lambdaUpdate(VpnMember.class)
                        .set(VpnMember::getExpirTime, member.getExpirTime().plusDays(days))
                        .set(VpnMember::getIsMember, 0)
                        .eq(VpnMember::getId, member.getId());
                boolean isSuccess = vpnMemberService.update(wrapperUPdate);
                MemberRechargeflow flow = new MemberRechargeflow();
                flow.setCreateTime(LocalDateTime.now());
                flow.setOp("开通会员");
                flow.setIsmember(0);
                flow.setLastExpiretime(member.getExpirTime());
                flow.setDays(days);
                flow.setNewExpiretime(member.getExpirTime().plusDays(days));
                flow.setRemark("");
                flow.setHashDeal(hashcode);//没有哈希交易
                flow.setMemId(member.getId());
                flow.setOrderId(ReturnCard.ReturnDatetimeAndGuid());
                memberRechargeflowService.AddMemberRechargeflow(flow);
                if (false == isSuccess) {
                    throw new BizException(1, "注单失败");
                }
            }

        }

    }
}
