package com.main.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.exception.BizException;
import com.common.util.StringUtils;
import com.main.Entity.SystemConfig;
import com.main.dao.SystemConfigMapper;
import com.main.manage.requestbodyvo.GetSystemConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class SystemConfigService extends ServiceImpl<SystemConfigMapper, SystemConfig> {

    public List<SystemConfig> getAllSystemconfig() {
        LambdaQueryWrapper<SystemConfig> wrapper = Wrappers.lambdaQuery(SystemConfig.class);
        List<SystemConfig> lst = this.list(wrapper);
        return lst;
        //return result;
    }

    public SystemConfig GetSystemConfig(String key) {
        SystemConfig config = new SystemConfig();
        LambdaQueryWrapper<SystemConfig> wrapper = Wrappers.lambdaQuery(SystemConfig.class)
                .eq(StringUtils.isNotBlank(key), SystemConfig::getSystemKey, key);
        List<SystemConfig> lst = this.list(wrapper);
        if (lst.size() > 0) {
            config = lst.get(0);
        }
        return config;
    }

    public Integer EditSystemConfigValue(GetSystemConfigVO vo) {
        try {
            if (StringUtils.isNotBlank(vo.getSystemconfigValue())) {
                LambdaUpdateWrapper<SystemConfig> wrapper = Wrappers.lambdaUpdate(SystemConfig.class)
                        .set(StringUtils.isNotBlank(vo.getSystemconfigValue()), SystemConfig::getSystemValue, vo.getSystemconfigValue())
                        .eq(SystemConfig::getSystemKey, vo.getSystemconfigName());
                boolean isSuccess = this.update(wrapper);
                if (false == isSuccess) {
                    throw new BizException(1, "编辑系统配置失败");
                } else {
                    return 0;
                }
            } else {
                throw new BizException(1, "值不能为空");
            }
        }catch (Exception e) {
            log.info("关于我们保存失败",e);
            throw new BizException(1, "关于我们保存失败");
        }
    }

    public Integer SecuritySettings(GetSystemConfigVO vo) {
        LambdaUpdateWrapper<SystemConfig> wrapper = Wrappers.lambdaUpdate(SystemConfig.class)
                .set(SystemConfig::getSystemValue, vo.getSystemconfigValue())
                .eq(SystemConfig::getSystemKey, vo.getSystemconfigName());
        boolean isSuccess = this.update(wrapper);
        if (false == isSuccess) {
            throw new BizException(1, "编辑安全设置失败");
        } else {
            return 0;
        }
    }

    public SystemConfig QurySecuritySettings() {
        return GetSystemConfig("SecuritySettings");
    }

    public SystemConfig QueryRateUSDT() {
        return GetSystemConfig("RateUSDT");
    }

}
