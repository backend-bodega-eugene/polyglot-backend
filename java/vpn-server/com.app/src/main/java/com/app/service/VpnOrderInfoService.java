package com.app.service;

import com.app.Entity.*;
import com.app.controller.dto.IdDTOVO;
import com.app.controller.dto.UserQueryRequestParam;
import com.app.controller.dto.UserUploadOrderImg;
import com.app.dao.VpnOrderinfoMapper;
import com.app.manage.RefLocalDateTime;
import com.app.manage.ReturnCard;
import com.app.controller.dto.VpnOrderInfoBookingRequestParam;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class VpnOrderInfoService extends ServiceImpl<VpnOrderinfoMapper, VpnOrderinfo> {

    @Autowired
    AppMemberService appMemberService;
    @Autowired
    MemberRechargeflowService memberRechargeflowService;
    @Autowired
    VpnPackageService vpnPackageService;
    @Autowired
    PayInfoService payInfoService;
    @Autowired
    PayTypeService payTypeService;
    @Autowired
    SystemConfigService systemConfigService;

    public List<VpnOrderinfo> VpnOrderInfoQuery(UserQueryRequestParam dto, Long id) {
        LocalDateTime startTime = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.now().with(LocalTime.MAX);
        if (null == id) {
            throw new BizException(1, "id不能为空");
        }
        if (id <= 0) {
            throw new BizException(1, "id是非法的");
        }
        if (dto.getType() != -1) {

            switch (dto.getType()) {
                case 0:
                    startTime = LocalDate.now().atTime(LocalTime.MIN);
                    endTime = LocalDate.now().atTime(LocalTime.MAX);
                    break;
                case 1:
                    startTime = LocalDate.now().minusDays(7).atTime(LocalTime.MIN);
                    endTime = LocalDate.now().atTime(LocalTime.MAX);

                    break;
                case 2:
                    startTime = LocalDate.now().minusDays(30).atTime(LocalTime.MIN);
                    endTime = LocalDate.now().atTime(LocalTime.MAX);
                    break;
                case 3:
                    if (dto.getStartDatetime() == null || dto.getEndDatetime() == null) {
                        throw new BizException(1, "选择自定义时,请传入时间");
                    } else {
                        startTime = dto.getStartDatetime().atTime(LocalTime.MIN);
                        endTime = dto.getEndDatetime().atTime(LocalTime.MAX);

                    }
                    break;
                default:
                    break;
            }
        }
        LambdaQueryWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(id != null, VpnOrderinfo::getMemId, id)
                .ge(dto.getStartDatetime() != null, VpnOrderinfo::getOrderCreatetime, startTime)
                .le(dto.getEndDatetime() != null, VpnOrderinfo::getOrderCreatetime, endTime)
                .eq(dto.getOrderStatus() != -1, VpnOrderinfo::getOrderStatus, dto.getOrderStatus())
                .orderByDesc(VpnOrderinfo::getOrderCreatetime);
        return this.list(wrapper);
    }

    public String GetMemberWalletAddress(long memid) {
        if (memid <= 0) {
            throw new BizException(1, "非法会员id");
        }
        String memberWalletAddress = "";
        LambdaQueryWrapper<VpnMember> wrapper = Wrappers.lambdaQuery(VpnMember.class)
                .eq(VpnMember::getId, memid)
                .select(VpnMember::getTrcAddress, VpnMember::getId);
        List<VpnMember> lst = appMemberService.list(wrapper);
        if (lst.size() > 0) {
            memberWalletAddress = lst.get(0).getTrcAddress();
            if (!StringUtils.isNotBlank(memberWalletAddress)) {
                //调用接口获取钱包地址
                return GetWalletAddrenss();
            } else {

                return memberWalletAddress;
            }
        } else {
            throw new BizException(1, "用户不存在");

        }
    }

    public Boolean checkOrder(Long memid) {
        LambdaQueryWrapper<VpnOrderinfo> lambdaQueryWrapper = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(VpnOrderinfo::getOrderStatus, 0)
                .eq(VpnOrderinfo::getMemId, memid);
        VpnOrderinfo info = this.getOne(lambdaQueryWrapper);
        if (info != null) {
            return false;
        } else {
            return true;
        }
    }

    public Boolean cancelOrder(Long orderid, Long memid) {
        LambdaUpdateWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaUpdate(VpnOrderinfo.class)
                .set(VpnOrderinfo::getOrderStatus, 2)
                .set(VpnOrderinfo::getConfirmCreatetime, LocalDateTime.now())
                .eq(VpnOrderinfo::getId, orderid)
                .eq(VpnOrderinfo::getOrderStatus, 0)
                .eq(VpnOrderinfo::getMemId, memid);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            var orderinfo = this.getById(orderid);
            if (orderinfo.getOrderStatus() != 0) {
                throw new BizException(1, "订单状态已更改,不能再取消,请刷新");
            }
            throw new BizException(1, "取消订单失败");
        } else {
            return true;
        }
    }

    public Boolean uploadImg(UserUploadOrderImg vo, Long memid) {
        var orderinfo = this.getById(vo.getId());
        if (orderinfo == null) {
            throw new BizException(1, "订单不存在");
        }
        if (StringUtils.isNotBlank(orderinfo.getUserImage())) {
            throw new BizException(1, "不能重复上传凭证");
        }
        LambdaUpdateWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaUpdate(VpnOrderinfo.class)
                .set(VpnOrderinfo::getUserImage, vo.getUserImage())
                .eq(VpnOrderinfo::getId, vo.getId())
                .eq(VpnOrderinfo::getMemId, memid);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "上传付款凭证失败");
        } else {
            return true;
        }
    }

    public VpnOrderinfo VpnOrderInfoBooking(VpnOrderInfoBookingRequestParam param, Long memid) {
        return VpnOrderInfoBookingLock(param, memid);
    }

    private synchronized VpnOrderinfo VpnOrderInfoBookingLock(VpnOrderInfoBookingRequestParam param, Long memid) {

        //判断字段合法性
        //查询会员信息
        VpnMember member = appMemberService.getById(memid);
        if(member == null){
            log.error("用户id:"+memid);
            throw new BizException(1, "请登录!");
        }
        //判断该会员是否有订单
        Boolean ischecksuccess = checkOrder(memid);
        if (!ischecksuccess) {
            throw new BizException(1, "你还有订单没处理!");
        }
        //查询套餐信息
        VpnPackage vpnPackage = vpnPackageService.getById(param.getMempackageId());
        //查询通道信息
        PayInfo info = payInfoService.getById(param.getPayId());
        if (info == null) {
            throw new BizException(1, "支付通道参数错误,支付id不存在!");
        }
        PayType typeInfo = payTypeService.getById(info.getTypeId());
        //拼装信息
        VpnOrderinfo orderinfo = new VpnOrderinfo();

        orderinfo.setMemId(member.getId());
        orderinfo.setMemName(member.getMemName());
        orderinfo.setTypeId(typeInfo.getId());
        orderinfo.setTypeName(typeInfo.getTypeName());
        orderinfo.setPayId(info.getId());
        orderinfo.setPayName(info.getPayName());
        orderinfo.setMempackageId(vpnPackage.getId());
        orderinfo.setOrderGuid(ReturnCard.GetSingleton().ReturnDatetimeAndGuid());
        orderinfo.setOrderAmount(vpnPackage.getMempackageAmount());
        orderinfo.setRecieveAddress(info.getRecieveAddress());
        orderinfo.setOrderStatus(0);
        orderinfo.setOrderCreatetime(LocalDateTime.now());
        orderinfo.setMempackageQuantity(vpnPackage.getMempackageQuantity());
        orderinfo.setLastMemberdate(member.getExpirTime());
        orderinfo.setExpireDate(member.getExpirTime().isBefore(LocalDateTime.now()) ?
                LocalDateTime.now().plusDays(vpnPackage.getMempackageQuantity()) :
                member.getExpirTime().plusDays(vpnPackage.getMempackageQuantity()));
        orderinfo.setRecieveQr(info.getRecieveQr());
        orderinfo.setRecieveName(info.getRecieveName());
        orderinfo.setBank(info.getBank());
        orderinfo.setRecieveAccount(info.getRecieveAccount());
        orderinfo.setRecieveProtocol(info.getRecieveProtocol());

        BigDecimal t = new BigDecimal(StringUtils.isNotBlank(systemConfigService.QueryRateUSDT().getSystemValue()) ?
                systemConfigService.QueryRateUSDT().getSystemValue() :
                "1");
        orderinfo.setUsdt(vpnPackage.getMempackageAmount().divide(t, RoundingMode.CEILING));
        //下单
        if (this.save(orderinfo)) {
            return orderinfo;
        } else {

            throw new BizException(1, "下单失败");
        }
    }

    private String GetWalletAddrenss() {
        //尚未实现
        return "用户钱包地址接口尚未对接实现";
    }

}
