package com.app.controller;

import com.app.Entity.VpnPackage;
import com.app.controller.base.BaseController;
import com.app.controller.dto.IdDtovpnpacka;
import com.app.service.VpnPackageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Api(value = "会员套餐", tags = {"套餐查询"})
@RestController
@RequestMapping(value = "/app/vpnpackage", method = {RequestMethod.POST})
public class VpnPackageController extends BaseController {
    @Autowired
    VpnPackageService vpnPackageService;

    @Operation(summary = "所有在售套餐查询")
    @RequestMapping("/query")
    public List<VpnPackage> VpnPackageQuery() {

        return vpnPackageService.VpnPackageQuery();
    }

    @Operation(summary = "单个套餐查询,按套餐id查询")
    @RequestMapping("/queryone")
    public VpnPackage VpnPackageQueryOne(@RequestBody IdDtovpnpacka vo) {

        return vpnPackageService.VpnPackageQueryOne(vo.getVpnpackageid());
    }
}
