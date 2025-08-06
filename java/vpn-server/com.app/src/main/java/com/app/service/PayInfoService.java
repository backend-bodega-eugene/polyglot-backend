package com.app.service;

import com.app.Entity.PayType;
import com.app.controller.dto.responseVO.PageResultResponseVO;
import com.app.controller.dto.responseVO.PayInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.Entity.PayInfo;
import com.app.dao.PayInfoMapper;
import com.common.util.BeanCoper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayInfoService extends ServiceImpl<PayInfoMapper, PayInfo> implements IService<PayInfo> {
    @Autowired
    PayTypeService payTypeService;

    public List<PayInfoVO> GetallPayTypeResponseVOS() {
        List<PayInfoVO> list;
        LambdaQueryWrapper<PayInfo> wrapper = Wrappers.lambdaQuery(PayInfo.class)
                .eq(PayInfo::getStatus, true);
        var payInfoList = this.list(wrapper);
        var lst = payTypeService.list();
        list = BeanCoper.clone(PayInfoVO.class, lst);
        list.forEach(paytypeInfo -> {
            paytypeInfo.setPayInfoList(payInfoList
                    .stream()
                    .filter(item -> item.getTypeId().equals(paytypeInfo.getId()))
                    .collect(Collectors.toList()));
        });
        for (int i = list.size() - 1; i >= 0; i--) {
            if (null == list.get(i).getPayInfoList()) {
                list.remove(i);
                continue;
            }
            if (list.get(i).getPayInfoList().size() == 0) {
                list.remove(i);
                //continue;
            }

        }
        return list;
    }

    public List<PayInfoVO> GetallPayTypeResponseVOS(BigDecimal amount) {
        List<PayInfoVO> list;
        var lst = payTypeService.list();
        list = BeanCoper.clone(PayInfoVO.class, lst);
        LambdaQueryWrapper<PayInfo> wrapper = Wrappers.lambdaQuery(PayInfo.class)
                .eq(PayInfo::getStatus, true)
                .le(PayInfo::getLowLimit, amount)
                .ge(PayInfo::getHighLimit, amount).or()
                .eq(PayInfo::getHighLimit, 0);
        // .eq(PayInfo::getHighLimit,0);
        var payInfoList = this.list(wrapper);
        list.forEach(paytypeInfo -> {
            var temp = payInfoList
                    .stream()
                    .filter(item -> item.getTypeId().equals(paytypeInfo.getId()))
                    .collect(Collectors.toList());
            paytypeInfo.setPayInfoList(temp);
        });
        for (int i = list.size() - 1; i >= 0; i--) {
            if (null == list.get(i).getPayInfoList()) {
                list.remove(i);
                continue;
            }
            if (list.get(i).getPayInfoList().size() == 0) {
                list.remove(i);
                //continue;
            }

        }
        return list;
    }

    public PayInfo getOne(Long id) {
        return this.getById(id);
    }
}
