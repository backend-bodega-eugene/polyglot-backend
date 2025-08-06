package com.main.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.exception.BizException;
import com.common.util.BeanCoper;
import com.main.Entity.VpnPackage;
import com.main.dao.VpnPackageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.manage.requestbodyvo.VpnPackageRequestVO;
import com.main.manage.responseVO.VpnPackageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VpnPackageService extends ServiceImpl<VpnPackageMapper, VpnPackage> implements IService<VpnPackage> {
    public List<VpnPackageVO> VpnPackageQuery() {
        LambdaQueryWrapper<VpnPackage> wrapper = Wrappers.lambdaQuery(VpnPackage.class)
                .orderByDesc(VpnPackage::getSort);
        return BeanCoper.clone(VpnPackageVO.class, this.list(wrapper));
        //  return result;
    }

    public VpnPackage VpnPackageQueryOne(long id) {
        LambdaQueryWrapper<VpnPackage> wrapper = Wrappers.lambdaQuery(VpnPackage.class)
                .eq(VpnPackage::getMempackageStatus, 0)
                .eq(VpnPackage::getId, id);
        List<VpnPackage> lst = this.list(wrapper);
        if (lst.size() > 0) {
            return lst.get(0);
        }
        throw new BizException(1, "没有套餐数据");

    }

    public VpnPackage AddVpnPackage(VpnPackageRequestVO vpnpackage) {
        VpnPackage pa = new VpnPackage();
        pa.setMempackageType(1);
        pa.setMempackageAmount(vpnpackage.getMempackageAmount());
        pa.setMempackageQuantity(vpnpackage.getMempackageQuantity());
        pa.setSort(vpnpackage.getSort());
        pa.setMempackageCurrency("元");
        pa.setMempackageDescription("套餐描述");
        pa.setMempackageTitle("套餐描述");
        pa.setMempackageCount(1);
        pa.setCreate_time(LocalDateTime.now());
        pa.setRemark("备注");
        pa.setMempackageStatus(1);
        pa.setMempackageImages("/main/other/ossb69d9b82-348d-5fdb-0a4f-5704264964cc.png");
        boolean isSuccess = this.save(pa);
        if (false == isSuccess) {
            throw new BizException(1, "删除国家失败");
        } else {
            return pa;
        }
    }

    public Integer EditVpnPackage(VpnPackageRequestVO vpnpackage) {

        if (vpnpackage.getId() <= 0) {
            throw new BizException(1, "id不合法");

        }
        if (vpnpackage.getMempackageAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(1, "金额不能小于0");

        }
        if (vpnpackage.getMempackageQuantity() < 0) {
            throw new BizException(1, "天数必须大于0");

        }
        LambdaUpdateWrapper<VpnPackage> wrapper = Wrappers.lambdaUpdate(VpnPackage.class)
                .set(vpnpackage.getMempackageAmount().compareTo(BigDecimal.ZERO) > 0, VpnPackage::getMempackageAmount, vpnpackage.getMempackageAmount())
                .set(vpnpackage.getMempackageQuantity() > 0, VpnPackage::getMempackageQuantity, vpnpackage.getMempackageQuantity())
                .set(VpnPackage::getSort, vpnpackage.getSort())
                // .set(VpnPackage::getMempackageImages, vpnpackage.getMempackageImages())
                .eq(VpnPackage::getId, vpnpackage.getId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "编辑套餐失败");
        } else {
            return 0;
        }
    }

    public Integer EditVpnPackageStatus(long vpnpackageid, Integer status) {
        LambdaUpdateWrapper<VpnPackage> wrapper = Wrappers.lambdaUpdate(VpnPackage.class)
                .set(VpnPackage::getMempackageStatus, status)
                .eq(VpnPackage::getId, vpnpackageid);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "编辑套餐状态失败");
        } else {
            return 0;
        }
    }

    public Integer DeleteVpnPackage(long pnPackageid) {
        LambdaQueryWrapper<VpnPackage> wrapper = Wrappers.lambdaQuery(VpnPackage.class)
                .eq(VpnPackage::getId, pnPackageid);
        boolean isSuccess = this.remove(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "删除套餐失败");
        } else {
            return 0;
        }
    }
}

