package com.app.controller;

import com.app.Entity.ApplicationInfo;
import com.app.Entity.ClientUpgrade;
import com.app.controller.base.BaseController;
import com.app.service.ApplicationInfoService;
import com.app.service.ClientUpgradeService;
import com.common.exception.BizException;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(value = "客户端相关", tags = {"客户端更新"})
@RestController
@RequestMapping(value = "/app/otherapplicationinfo", method = {RequestMethod.POST})
public class ApplicationInfoController extends BaseController {
    @Autowired
    ClientUpgradeService clientUpgradeService;
    @Autowired
    ApplicationInfoService applicationInfoService;

    @Operation(summary = "所有客户端信息查询")
    @RequestMapping("/query")
    public List<ApplicationInfo> ApplicationInfoServiceQuery() {
        return applicationInfoService.ApplicationInfoServiceQuery();
    }

    @Operation(summary = "检查客户端更新信息 0:ios,1:android,2:windows,3:macOS,4:Linux")
    @RequestMapping("/checkversion")
    public ClientUpgrade CheckVersion(int versionType) {

        return clientUpgradeService.CheckVersion(versionType);
    }

    @RequestMapping("/memberStatusError")
    public void memberStatusError() {
        throw new BizException(256, "用户已经被冻结");
        //  return clientUpgradeService.CheckVersion(versionType);
    }
}
