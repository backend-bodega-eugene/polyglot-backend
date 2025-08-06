package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.util.StringUtils;
import com.main.Entity.PayInfo;
import com.main.dao.PayInfoMapper;
import com.main.manage.responseVO.PageResultResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PayInfoService extends ServiceImpl<PayInfoMapper, PayInfo> implements IService<PayInfo> {

    public PageResultResponseVO<List<PayInfo>> GetallPayTypeResponseVOS(
            Long pageIndex
            , Long pageSize
            , Long typeid
            , String payname
            , Boolean isStatus) {
        Boolean istypeid = false;
        if (typeid != null) {
            if (typeid > 0) {
                istypeid = true;
            }
        }
        Boolean ispayname = false;
        if (StringUtils.isNotBlank(payname)) {
            ispayname = true;
        }
        PageResultResponseVO<List<PayInfo>> response = new PageResultResponseVO<>();
        LambdaQueryWrapper<PayInfo> wrapper = Wrappers.lambdaQuery(PayInfo.class)
                .eq(istypeid, PayInfo::getTypeId, typeid)
                .eq(ispayname, PayInfo::getPayName, payname)
                .eq(isStatus != null, PayInfo::getStatus, isStatus);
        Page pagination = new Page(pageIndex, pageSize);
        Page<PayInfo> page = super.page(pagination, wrapper);
        List<PayInfo> list = page.getRecords();
        response.setPageIndex(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setTotal(page.getTotal());
        response.setData(list);
        return response;
    }
}
