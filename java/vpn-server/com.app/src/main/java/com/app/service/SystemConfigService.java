package com.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.app.Entity.SystemConfig;
import com.app.dao.SystemConfigMapper;
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
    }

    public SystemConfig GetSystemConfig(String key) {
        SystemConfig config = new SystemConfig();
        LambdaQueryWrapper<SystemConfig> wrapper = Wrappers.lambdaQuery(SystemConfig.class)
                .eq(SystemConfig::getSystemKey, key);
        List<SystemConfig> lst = this.list(wrapper);
        if (lst.size() > 0) {
            config = lst.get(0);
        }
        return config;
    }

    public SystemConfig GetSystemConfigInviterRewardDays() {
        //邀请好友奖励时长
        return GetSystemConfig("InviterRewardDays");
    }

    public SystemConfig GetSystemConfigInviteeBonusDays() {
        //注册(游客)赠送时长
        return GetSystemConfig("InviteeBonusDays");
    }

    public SystemConfig GetSystemConfigBIndMemberRewardDays() {
        //绑定时,被邀请人赠送时长
        return GetSystemConfig("BIndMemberRewardDays");
    }

    public SystemConfig QueryvpnConnectInfo() {
        return GetSystemConfig("vpnConnectInfo");
    }

    public SystemConfig QueryvpnConnectInfoAI() {
        return GetSystemConfig("vpnConnectInfoAI");
    }

    public SystemConfig QueryRateUSDT() {

        //查询汇率
        return GetSystemConfig("RateUSDT");
    }

    public SystemConfig QueryNumberOfClients() {
        //登录设置 最多登录的客户端数量
        return GetSystemConfig("NumberOfClients");
    }
}
