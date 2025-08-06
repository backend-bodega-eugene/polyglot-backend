package com.app.controller;

import com.app.Entity.SystemConfig;
import com.app.service.SystemConfigService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Api(value = "系统配置", tags = {"系统配置"})
@RestController
@RequestMapping(value = "/app/systemconfig", method = {RequestMethod.POST})
public class SystemConfigController {

    @Autowired
    SystemConfigService systemConfigService;

    @Operation(summary = "系统配置查询")
    @RequestMapping("/query")
    public List<SystemConfig> getAllSystemconfig() {
        return systemConfigService.getAllSystemconfig();
    }

    @Operation(summary = "查询一个系统配置")
    @RequestMapping("/querykey")
    public SystemConfig GetSystemConfig(String key) {
        return systemConfigService.GetSystemConfig(key);
    }

    @Operation(summary = "查询汇率")
    @RequestMapping("/queryrateusdt")
    public BigDecimal queryRateUSDT() {
        SystemConfig config = systemConfigService.QueryRateUSDT();
        return new BigDecimal(config.getSystemValue());
    }
}
