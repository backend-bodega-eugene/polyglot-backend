package com.main.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.Entity.PayInfo;
import com.main.Entity.PayType;
import com.main.dao.PayTypeMapper;
import com.main.manage.responseVO.PayInfoResponseVO;
import com.main.manage.responseVO.PayTypeResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PayTypeService extends ServiceImpl<PayTypeMapper, PayType> implements IService<PayType> {
    @Autowired
    PayInfoService payInfoService;

    public List<PayTypeResponseVO> GetallPayTypeResponseVOS() {
        List<PayTypeResponseVO> lst = new ArrayList<>();
        var payTypes = this.list();
        var payinfos = payInfoService.list();
        payTypes.forEach(item -> {
            PayTypeResponseVO vo = new PayTypeResponseVO();
            vo.setValue(item.getId());
            vo.setLabel(item.getTypeName());
            List<PayInfoResponseVO> currentpayinfos = new ArrayList<>();
            var pays = payinfos.stream().filter(item2 -> item2.getTypeId() == item.getId()).collect(Collectors.toList());
            pays.forEach(item3 -> {
                PayInfoResponseVO vo1 = new PayInfoResponseVO();
                vo1.setValue(item3.getId());
                vo1.setLabel(item3.getPayName());
                currentpayinfos.add(vo1);
            });
            vo.setChildren(currentpayinfos);
            lst.add(vo);

        });
        return lst;
    }
}
