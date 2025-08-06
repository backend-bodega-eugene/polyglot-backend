package com.app.service;

import com.app.Entity.VpnPackage;
import com.app.dao.VpnPackageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VpnPackageService extends ServiceImpl<VpnPackageMapper, VpnPackage> {
    public List<VpnPackage> VpnPackageQuery() {

        LambdaQueryWrapper<VpnPackage> wrapper = Wrappers.lambdaQuery(VpnPackage.class)
                .eq(VpnPackage::getMempackageStatus, 0);
        return this.list(wrapper);
    }

    public VpnPackage VpnPackageQueryOne(long id) {
        LambdaQueryWrapper<VpnPackage> wrapper = Wrappers.lambdaQuery(VpnPackage.class)
                .eq(VpnPackage::getMempackageStatus, 0)
                .eq(VpnPackage::getId, id);
        List<VpnPackage> lst = this.list(wrapper);
        if (lst.size() > 0) {
            return lst.get(0);
        }
        throw new BizException(1, "没有找到套餐信息");
    }
}

