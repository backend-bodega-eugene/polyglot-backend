package com.main.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.BizException;
import com.common.util.StringUtils;
import com.main.Entity.MemberRechargeflow;
import com.main.Entity.VpnMember;
import com.main.Entity.VpnOrderinfo;
import com.main.dao.VpnOrderinfoMapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.manage.ReturnCard;
import com.main.manage.requestbodyvo.OrderRemakrParamVO;
import com.main.manage.requestbodyvo.OrderStatusParamVO;
import com.main.manage.responseVO.OrderListVHistoryO;
import com.main.manage.responseVO.OrderListVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.responseVO.SumAllTotalVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class VpnOrderinfoService extends ServiceImpl<VpnOrderinfoMapper, VpnOrderinfo> implements IService<VpnOrderinfo> {
    @Autowired()
    VpnMemberService vpnMemberService;
    @Autowired()
    SystemConfigService systemConfigService;
    @Autowired
    MemberRechargeflowService memberRechargeflowService;

    public PageResultResponseVO<OrderListVO> GetAllorderInfo(
            Long pageIndex,
            Long pageSize,
            String orderguid,
            String memId,
            String memName,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            Long[] paysId) {
        long memidl = -1;
        if (StringUtils.isNotBlank(memId)) {
            if (isNumeric(memId)) {

                memidl = Long.parseLong(memId);
            } else {
                throw new BizException(1, "用户id必须是数字");

            }

        }
        if (paysId == null) {
            paysId = new Long[]{
                    -1L, -1L
            };
        }
        if (paysId.length == 0) {
            paysId = new Long[]{
                    -1L, -1L
            };
        }
        if (paysId.length == 1) {
            paysId = new Long[]{
                    paysId[0], -1L
            };
        }

        PageResultResponseVO<OrderListVO> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(StringUtils.isNotBlank(orderguid), VpnOrderinfo::getOrderGuid, orderguid)
                .eq(StringUtils.isNotBlank(memId), VpnOrderinfo::getMemId, memidl)
                .eq(StringUtils.isNotBlank(memName), VpnOrderinfo::getMemName, memName)
                .eq(paysId.length > 1 && paysId[0] > 0, VpnOrderinfo::getTypeId, paysId[0])
                .eq(paysId.length > 1 && paysId[1] > 0, VpnOrderinfo::getPayId, paysId[1])
                .ge(startDatetime != null, VpnOrderinfo::getOrderCreatetime, startDatetime)
                .le(endDatetime != null, VpnOrderinfo::getOrderCreatetime, endDatetime)
                .eq(VpnOrderinfo::getOrderStatus, 0)
                .orderByAsc(VpnOrderinfo::getOrderCreatetime);
        var totallst = this.list(wrapper);
        Page pagination = new Page(pageIndex, pageSize);
        Page<VpnOrderinfo> page = super.page(pagination, wrapper);
        List<VpnOrderinfo> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        //组装数据
        OrderListVO vo = new OrderListVO();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < totallst.size(); i++) {

            total = total.add(totallst.get(i).getOrderAmount());

        }

        vo.setTotal(total);
        vo.setList(list);
        response.setData(vo);
        return response;
    }

    public PageResultResponseVO<OrderListVHistoryO> GetAllorderInfoHistory(
            Long pageIndex,
            Long pageSize,
            String orderguid,
            Long memId,
            String memName,
            Integer orderStatus,
            LocalDateTime startDatetime,
            LocalDateTime endDatetime,
            Long[] paysId) {
        if (paysId == null) {
            paysId = new Long[]{
                    -1L, -1L
            };
        }
        if (paysId.length == 0) {
            paysId = new Long[]{
                    -1L, -1L
            };
        }
        if (paysId.length == 1) {
            paysId = new Long[]{
                    paysId[0], -1L
            };
        }
        PageResultResponseVO<OrderListVHistoryO> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(StringUtils.isNotBlank(orderguid), VpnOrderinfo::getOrderGuid, orderguid)
                .eq(memId > 0, VpnOrderinfo::getMemId, memId)
                .eq(orderStatus > -1, VpnOrderinfo::getOrderStatus, orderStatus)
                .eq(StringUtils.isNotBlank(memName), VpnOrderinfo::getMemName, memName)
                .eq(paysId.length > 1 && paysId[0] > 0, VpnOrderinfo::getTypeId, paysId[0])
                .eq(paysId.length > 1 && paysId[1] > 0, VpnOrderinfo::getPayId, paysId[1])
                .ge(startDatetime != null, VpnOrderinfo::getOrderCreatetime, startDatetime)
                .le(endDatetime != null, VpnOrderinfo::getOrderCreatetime, endDatetime)
                .ne(VpnOrderinfo::getOrderStatus, 0)
                .orderByDesc(VpnOrderinfo::getOrderCreatetime);
        var totallst = this.list(wrapper);
        Page pagination = new Page(pageIndex, pageSize);
        Page<VpnOrderinfo> page = super.page(pagination, wrapper);
        List<VpnOrderinfo> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        //组装数据
        OrderListVHistoryO vo = new OrderListVHistoryO();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal cancel = BigDecimal.ZERO;
        for (int i = 0; i < totallst.size(); i++) {
            if (totallst.get(i).getOrderStatus() == 1) {
                total = total.add(totallst.get(i).getOrderAmount());
            } else if (totallst.get(i).getOrderStatus() == 3 || totallst.get(i).getOrderStatus() == 2) {

                cancel = cancel.add(totallst.get(i).getOrderAmount());
            }
        }
        vo.setSuccess(total);
        vo.setCancel(cancel);
        vo.setList(list);
        response.setData(vo);
        return response;
    }
    public SumAllTotalVO sumAllOrder() {
        LambdaQueryWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(VpnOrderinfo::getOrderStatus, 1);
        var orders = this.list(wrapper);
        BigDecimal total = BigDecimal.ZERO;
        for (var order : orders) {
            total = total.add(order.getOrderAmount());
        }
        LambdaQueryWrapper<VpnOrderinfo> wrapper1 = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(VpnOrderinfo::getOrderStatus, 2);
        var orders1 = this.list(wrapper1);
        BigDecimal total1 = BigDecimal.ZERO;
        for (var order : orders1) {
            total1 = total1.add(order.getOrderAmount());
        }
        SumAllTotalVO vo = new SumAllTotalVO();
        vo.setSuccess(total);
        vo.setCancel(total1);
        return vo;
    }

    public BigDecimal handingAllOrder() {
        LambdaQueryWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(VpnOrderinfo::getOrderStatus, 0);
        var orders = this.list(wrapper);
        BigDecimal total = BigDecimal.ZERO;
        for (var order : orders) {
            total = total.add(order.getOrderAmount());
        }
        return total;
    }

    public Boolean confirmOrder(OrderStatusParamVO vo) {
        LambdaUpdateWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaUpdate(VpnOrderinfo.class)
                .set(VpnOrderinfo::getOrderStatus, 1)
                .set(VpnOrderinfo::getConfirmCreatetime, LocalDateTime.now())
                .eq(VpnOrderinfo::getId, vo.getId())
                .eq(VpnOrderinfo::getOrderStatus, 0);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            var orderinfo = this.getById(vo.getId());
            if (orderinfo.getOrderStatus() != 0) {
                throw new BizException(1, "订单状态已更改,不能再确认,请刷新");
            }
            throw new BizException(1, "确认订单失败");
        } else {
            //查出订单所有信息
            var orderinfo = this.getById(vo.getId());
            //新增会员流水表
            MemberRechargeflow flow = new MemberRechargeflow();
            flow.setOrderId(ReturnCard.ReturnDatetimeAndGuid());
            flow.setCreateTime(LocalDateTime.now());
            flow.setOpId(1);
            flow.setOp("开通会员");
            flow.setIsmember(0);
            flow.setLastExpiretime(orderinfo.getLastMemberdate());
            flow.setDays(orderinfo.getMempackageQuantity());
            flow.setNewExpiretime(orderinfo.getExpireDate());
            flow.setRemark("");
            flow.setHashDeal("");
            flow.setMemId(orderinfo.getMemId());
            flow.setMemName(orderinfo.getMemName());
            memberRechargeflowService.save(flow);
            //增加对应天数
            //查询用户,并且增加用户的天数
            VpnMember vpnMember = vpnMemberService.getById(orderinfo.getMemId());
            vpnMember.setExpirTime(orderinfo.getExpireDate());
            vpnMember.setIsMember(0);
            vpnMember.setMemType(0);
            vpnMemberService.updateById(vpnMember);
            return true;
        }
    }

    public Boolean cancelOrder(OrderRemakrParamVO vo) {
        if (StringUtils.isNotBlank(vo.getRemark())) {
            if (vo.getRemark().length() > 200) {

                throw new BizException(1, "备注长度不能超过200个字");
            }
        }
        LambdaUpdateWrapper<VpnOrderinfo> wrapper = Wrappers.lambdaUpdate(VpnOrderinfo.class)
                .set(VpnOrderinfo::getOrderStatus, 2)
                .set(VpnOrderinfo::getConfirmCreatetime, LocalDateTime.now())
                .set(VpnOrderinfo::getRemark, vo.getRemark())
                .eq(VpnOrderinfo::getId, vo.getId())
                .eq(VpnOrderinfo::getOrderStatus, 0);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            var orderinfo = this.getById(vo.getId());
            if (orderinfo.getOrderStatus() != 0) {
                throw new BizException(1, "订单状态已更改,不能再取消,请刷新");
            }
            throw new BizException(1, "取消订单失败");
        } else {
            return true;
        }
    }

    public void updateOrderStatus() {
        LambdaUpdateWrapper<VpnOrderinfo> updateWrapper = Wrappers.lambdaUpdate(VpnOrderinfo.class)
                .set(VpnOrderinfo::getOrderStatus, 3)
                .set(VpnOrderinfo::getConfirmCreatetime, LocalDateTime.now())
                .eq(VpnOrderinfo::getOrderStatus, 0)
                .le(VpnOrderinfo::getOrderCreatetime, LocalDateTime.now().minusMinutes(30));
        this.update(updateWrapper);
    }

    public boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public Integer getallMember() {
        QueryWrapper<VpnOrderinfo> wrapper5 = new QueryWrapper<VpnOrderinfo>();
        // wrapper5.ge(VpnOrderinfo::getExpireDate,LocalDateTime.now());
        wrapper5.select("DISTINCT mem_id ");
        wrapper5.ge("expire_date", LocalDateTime.now());
        return this.list(wrapper5).size();

    }

    public BigDecimal getallMemberAmount() {
        LambdaQueryWrapper<VpnOrderinfo> wrapper5 = Wrappers.lambdaQuery(VpnOrderinfo.class)
                .eq(VpnOrderinfo::getOrderStatus, 1);
        var lst = this.list(wrapper5);
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < lst.size(); i++) {

            total = total.add(lst.get(i).getOrderAmount());
        }
        return total;

    }

}