package com.main.controller;

import com.main.Entity.SystemConfig;
import com.main.manage.requestbodyvo.GetSystemConfigVO;
import com.main.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "系统配置", tags = {"系统配置"})
@RestController
@RequestMapping(value = "/backend/systemconfig", method = {RequestMethod.POST})
public class SystemConfigController {

    @Autowired
    SystemConfigService systemConfigService;

    @Operation(summary = "所有系统配置 查询这个可以得到所有配置")
    @RequestMapping("/query")
    public List<SystemConfig> VpnPackageQuery() {
        return systemConfigService.getAllSystemconfig();
    }

    @Operation(summary = "查询系统配置 忽略systemconfigValue, 传systemconfigName即可")
    @RequestMapping("/querykey")
    public SystemConfig GetSystemConfig(@RequestBody GetSystemConfigVO vo) {
        return systemConfigService.GetSystemConfig(vo.getSystemconfigName());
    }

    @Operation(summary = "修改注册赠送时长 忽略systemconfigName, 传systemconfigValue即可")
    @RequestMapping("/inviteebonusdays")
    public Integer RegisterRewardDays(@RequestBody GetSystemConfigVO vo) {
        vo.setSystemconfigName("InviteeBonusDays");
        return systemConfigService.EditSystemConfigValue(vo);
    }

    @Operation(summary = "修改邀请好友奖励时长 忽略systemconfigName, 传systemconfigValue即可")
    @RequestMapping("/inviterrewarddays")
    public Integer InviterRewardDays(@RequestBody GetSystemConfigVO vo) {
        vo.setSystemconfigName("InviterRewardDays");
        return systemConfigService.EditSystemConfigValue(vo);
    }

    @Operation(summary = "修改登录设置 忽略systemconfigName, 传systemconfigValue即可")
    @RequestMapping("/numberofclients")
    public Integer NumberOfClients(@RequestBody GetSystemConfigVO vo) {
        vo.setSystemconfigName("NumberOfClients");
        return systemConfigService.EditSystemConfigValue(vo);
    }

    @Operation(summary = "修改安全设置 忽略systemconfigName, 传systemconfigValue即可")
    @RequestMapping("/securitysettings")
    public Integer SecuritySettings(@RequestBody GetSystemConfigVO vo) {
        vo.setSystemconfigName("SecuritySettings");
        return systemConfigService.SecuritySettings(vo);
    }

    @Operation(summary = "查询安全设置")
    @RequestMapping("/querysecuritysettings")
    public SystemConfig QurySecuritySettings() {
        return systemConfigService.QurySecuritySettings();
    }

    @Operation(summary = "修改关于我们 忽略systemconfigName, 传systemconfigValue即可")
    @RequestMapping("/editaboutus")
    public Integer EditAboutUs(@RequestBody GetSystemConfigVO vo) {
        vo.setSystemconfigName("AboutUs");
        return systemConfigService.EditSystemConfigValue(vo);
    }

    @Operation(summary = "查询关于我们 忽略systemconfigName, 传systemconfigValue即可")
    @RequestMapping("/aboutus")
    public SystemConfig AboutUs() {
        GetSystemConfigVO vo = new GetSystemConfigVO();
        vo.setSystemconfigName("AboutUs");
        return systemConfigService.GetSystemConfig(vo.getSystemconfigName());
    }
}