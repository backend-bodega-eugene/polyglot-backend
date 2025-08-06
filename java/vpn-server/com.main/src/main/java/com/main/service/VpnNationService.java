package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.exception.BizException;
import com.common.result.Result;
import com.common.util.StringUtils;
import com.main.Entity.VpnLine;
import com.main.Entity.VpnNation;
import com.main.dao.VpnNationMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.main.manage.requestbodyvo.PageResultRequestVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.VpnNationRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VpnNationService extends ServiceImpl<VpnNationMapper, VpnNation> implements IService<VpnNation> {
    @Lazy
    @Autowired
    VpnLineLineService vpnLineLineService;

    public PageResultResponseVO<List<VpnNation>> GetAllNation(PageResultRequestVO query) {
        PageResultResponseVO<List<VpnNation>> response = new PageResultResponseVO();
        LambdaQueryWrapper<VpnNation> wrapper = Wrappers.lambdaQuery(VpnNation.class)
                .like(StringUtils.isNotBlank(query.getNationName()), VpnNation::getNationName, query.getNationName())
                .eq(query.getNationStatus() > -1, VpnNation::getNationStatus, query.getNationStatus())
                .orderByDesc(VpnNation::getSort);
        Page pagination = new Page(query.getPageIndex(), query.getPageSize());
        Page<VpnNation> page = super.page(pagination, wrapper);
        List<VpnNation> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setData(list);
        return response;
    }

    public Integer AddNation(VpnNationRequestVO quest) {

        Result<Integer> result = new Result<>();
        VpnNation vpnNation = new VpnNation();
        vpnNation.setNationIcon(quest.getNationIcon());
        vpnNation.setNationStatus(0);
        vpnNation.setSort(quest.getSort());
        vpnNation.setNationName(quest.getNationName());
        vpnNation.setRemark("_");
        boolean isSuccess = this.save(vpnNation);
        if (false == isSuccess) {
            throw new BizException(1, "添加国家失败");
        } else {
            return 0;
        }
    }

    public Integer EditNation(VpnNationRequestVO quest) {

        Result<Integer> result = new Result<>();
        LambdaUpdateWrapper<VpnNation> wrapper = Wrappers.lambdaUpdate(VpnNation.class)
                .set(StringUtils.isNotBlank(quest.getNationName()), VpnNation::getNationName, quest.getNationName())
                .set(StringUtils.isNotBlank(quest.getNationIcon()), VpnNation::getNationIcon, quest.getNationIcon())
                .set(VpnNation::getSort, quest.getSort())
                .eq(VpnNation::getId, quest.getId());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "修改国家失败");
        } else {
            return 0;
        }
    }

    public Integer EditNationStatus(long nationid, Integer status) {

        Result<Integer> result = new Result<>();
        LambdaUpdateWrapper<VpnNation> wrapper = Wrappers.lambdaUpdate(VpnNation.class)
                .set(VpnNation::getNationStatus, status)
                .eq(VpnNation::getId, nationid);
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "修改国家状态失败");
        } else {
            return 0;
        }
    }

    public Integer DeleteNation(long nationid) {
        //首先判断
        LambdaQueryWrapper<VpnLine> queryWrapper = Wrappers.lambdaQuery(VpnLine.class)
                .eq(VpnLine::getLineNationid, nationid);
        List<VpnLine> list = vpnLineLineService.list(queryWrapper);
        if (list != null) {
            if (list.size() > 0) {
                throw new BizException(1, "该国家下有节点,请全部删除节点后,再删除国家.");
            }
        }
        LambdaUpdateWrapper<VpnNation> wrapper = Wrappers.lambdaUpdate(VpnNation.class)
                .eq(VpnNation::getId, nationid);
        boolean isSuccess = this.remove(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "删除国家失败");
        } else {
            return 0;
        }
    }
}
