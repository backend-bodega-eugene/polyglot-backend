package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.util.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.main.Entity.MemberRechargeflow;
import com.main.Entity.VpnOrderinfo;
import com.main.dao.MemberRechargeflowMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.manage.ReturnCard;
import com.main.manage.responseVO.MemberRechargeflowVO;
import com.main.manage.responseVO.OrderListVO;
import com.main.manage.responseVO.PageResultResponseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MemberRechargeflowService extends ServiceImpl<MemberRechargeflowMapper, MemberRechargeflow> implements IService<MemberRechargeflow> {

    public boolean AddMemberRechargeflow(MemberRechargeflow flow) {

        return this.save(flow);
    }

    public PageResultResponseVO<List<MemberRechargeflow>> GetAllFlow(long memid, long pageIndex, long pageSize) {
        PageResultResponseVO<List<MemberRechargeflow>> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<MemberRechargeflow> wrapper = Wrappers.lambdaQuery(MemberRechargeflow.class)
                .eq(MemberRechargeflow::getMemId, memid);
        Page pagination = new Page(pageIndex, pageSize);
        Page<MemberRechargeflow> page = super.page(pagination, wrapper);
        List<MemberRechargeflow> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        for (int i = 0; i < list.size(); i++) {

            if(list.get(i).getNewExpiretime().isAfter(LocalDateTime.now())){

                list.get(i).setIsmember(0);
            }
        }
        response.setData(list);
        return response;
    }

    public PageResultResponseVO<MemberRechargeflowVO> GetAllFlowsByCondition(
            long pageIndex,
            long pageSize,
            LocalDateTime starttime,
            LocalDateTime endtime,
            Integer opId,
            String memName,
            long memId,
            String id) {

        PageResultResponseVO<MemberRechargeflowVO> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<MemberRechargeflow> wrapper = Wrappers.lambdaQuery(MemberRechargeflow.class)
                .eq(StringUtils.isNotBlank(memName), MemberRechargeflow::getMemName, memName)
                .eq(memId > 0, MemberRechargeflow::getMemId, memId)
                .eq(StringUtils.isNotBlank(id), MemberRechargeflow::getOrderId, id)
                .eq(opId > 0, MemberRechargeflow::getOp, getMaps(opId))
                .ge(starttime != null, MemberRechargeflow::getCreateTime, starttime)
                .le(endtime != null, MemberRechargeflow::getCreateTime, endtime)
                .ge(MemberRechargeflow::getDays, 0)
                .orderByDesc(MemberRechargeflow::getId);
        var totallst = this.list(wrapper);
        Page pagination = new Page(pageIndex, pageSize);
        Page<MemberRechargeflow> page = super.page(pagination, wrapper);
        List<MemberRechargeflow> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        MemberRechargeflowVO vo = new MemberRechargeflowVO();
        long total = 0;
        for (int i = 0; i < totallst.size(); i++) {

            total = total + totallst.get(i).getDays();
        }
        for (int i = 0; i < list.size(); i++) {

            if(list.get(i).getNewExpiretime().isAfter(LocalDateTime.now())){

                list.get(i).setIsmember(0);
            }
        }
        vo.setTotal(total);
        vo.setList(list);
        response.setData(vo);
        return response;
    }

    public Long totalBound() {

        var memberRechargeflows = this.list();
        Long total = 0L;
        for (var temp : memberRechargeflows) {
            total = total + temp.getDays();
        }
        return total;
    }

    private String getMaps(Integer key) {

        Map<Integer, String> map = new HashMap<>();
        map.put(1, "开通会员");
        map.put(2, "管理员续期");
        map.put(3, "扫码推荐赠送");
        map.put(4, "游客进入赠送");
        map.put(5, "注册赠送");
        map.put(6, "绑定赠送");
        map.put(7, "绑定赠送(被邀请人)");
        map.put(8, "绑定赠送(邀请人)");
        map.put(9, "扫码推荐赠送(被推荐人)");
        map.put(10, "扫码推荐赠送(推荐人)");
//        map.put(7, "绑定赠送");
        return map.get(key);
    }
//    @Schema(description = "开始时间")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//
//    private LocalDateTime startDatetime;

//    @Schema(description = "结束时间")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime endDateTime;
}
