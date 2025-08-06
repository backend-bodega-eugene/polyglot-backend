package com.app.service;

import com.app.Entity.ApplicationInfo;
import com.app.dao.ApplicationInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ApplicationInfoService extends ServiceImpl<ApplicationInfoMapper, ApplicationInfo> {

    public List<ApplicationInfo> ApplicationInfoServiceQuery() {
        LambdaQueryWrapper<ApplicationInfo> wrapper = new LambdaQueryWrapper<>(ApplicationInfo.class)
                .eq(ApplicationInfo::getApplicationStatus, 0);
        return this.list(wrapper);
    }

    public ApplicationInfo CheckApplicationStatus(String version, int i) {
        if (StringUtils.isBlank(version)) {
            throw new BizException(1, "版本号为空");
        }
        LambdaQueryWrapper<ApplicationInfo> lambdaQueryWrap = Wrappers.lambdaQuery(ApplicationInfo.class)
                .eq(ApplicationInfo::getApplicationType, i);
        List<ApplicationInfo> applications = this.list(lambdaQueryWrap);
        if (applications.size() == 0) {
            throw new BizException(1, "没有对应信息");
        }
        return applications.get(0);
    }
}