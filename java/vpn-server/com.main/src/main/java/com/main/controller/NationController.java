package com.main.controller;

import com.main.Entity.VpnNation;
import com.main.manage.requestbodyvo.PageResultRequestVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.VpnNationRequestVO;
import com.main.manage.requestbodyvo.EditNationStatusVO;
import com.main.service.VpnNationService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "国家管理", tags = {"国家添加,删除,修改,查询"})
@RestController
@RequestMapping(value = "/backend/nation", method = {RequestMethod.POST})
public class NationController {
    @Autowired
    VpnNationService vpnNationService;

    @Operation(summary = "国家查询")
    @RequestMapping("/query")
    public PageResultResponseVO<List<VpnNation>> NationQuery(@RequestBody PageResultRequestVO query) {
        return vpnNationService.GetAllNation(query);
    }

    @Operation(summary = "添加国家")
    @RequestMapping("/nation")
    public Integer AddNation(@RequestBody VpnNationRequestVO request) {
        return vpnNationService.AddNation(request);
    }

    @Operation(summary = "编辑国家")
    @RequestMapping("/edit")
    public Integer EditNation(@RequestBody VpnNationRequestVO request) {
        return vpnNationService.EditNation(request);
    }

    @Operation(summary = "修改国家状态")
    @RequestMapping("/editstatus")
    public Integer EditNationStatus(@RequestBody EditNationStatusVO statusVO) {
        return vpnNationService.EditNationStatus(statusVO.getNationid(), statusVO.getStatus());
    }

    @Operation(summary = "删除国家,传参数的时候忽略状态字段")
    @RequestMapping("/delete")
    public Integer DeleteNation(@RequestBody EditNationStatusVO statusVO) {
        return vpnNationService.DeleteNation(statusVO.getNationid());
    }
}
