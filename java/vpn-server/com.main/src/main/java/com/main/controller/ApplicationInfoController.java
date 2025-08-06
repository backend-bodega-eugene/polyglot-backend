package com.main.controller;

import com.main.Entity.ApplicationInfo;
import com.main.controller.base.BaseController;
import com.main.manage.requestbodyvo.ApplicationInfoRequestVO;
import com.main.service.ApplicationInfoService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "其他客户端", tags = {"其他客户端查询"})
@RestController
@RequestMapping(value = "/backend/otherapplicationinfo", method = {RequestMethod.POST})
public class ApplicationInfoController extends BaseController {
    @Autowired
    ApplicationInfoService applicationInfoService;

    @Operation(summary = "所有客户端信息查询")
    @RequestMapping("/query")
    public List<ApplicationInfo> ApplicationInfoServiceQuery() {
        return applicationInfoService.ApplicationInfoServiceQuery();
    }

    @Operation(summary = "添加客户端")
    @RequestMapping("/add")
    public Integer AddApplication(@RequestBody ApplicationInfoRequestVO request) {
        return applicationInfoService.AddApplication(request);
    }

    @Operation(summary = "修改客户端")
    @RequestMapping("/edit")
    public Integer EditApplication(@RequestBody ApplicationInfoRequestVO request) {
        return applicationInfoService.EditApplication(request);
    }

    @Operation(summary = "删除客户端 传入id")
    @RequestMapping("/delete")
    public Integer DeleteApplication(@RequestBody ApplicationInfoRequestVO request) {
        return applicationInfoService.DeleteApplication(request.getId());
    }

    @Operation(summary = "修改客户端状态 传入id和状态")
    @RequestMapping("/editstatus")
    public Integer EditApplicationStatus(@RequestBody ApplicationInfoRequestVO request) {
        return applicationInfoService.EditApplicationStatus(request.getId(), request.getApplicationStatus());
    }
}
