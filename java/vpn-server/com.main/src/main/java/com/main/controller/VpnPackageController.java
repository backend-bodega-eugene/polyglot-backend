package com.main.controller;

import com.main.Entity.VpnPackage;
import com.main.controller.base.BaseController;
import com.main.manage.requestbodyvo.VpnPackageRequestVO;
import com.main.manage.requestbodyvo.VpnPackageRequestVOStatus;
import com.main.manage.responseVO.VpnPackageVO;
import com.main.service.VpnPackageService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@Api(value = "会员套餐", tags = {"套餐查询"})
@RestController
@RequestMapping(value = "/backend/vpnpackage", method = {RequestMethod.POST})
public class VpnPackageController extends BaseController {
    @Autowired
    VpnPackageService vpnPackageService;

    @Operation(summary = "所有在售套餐查询")
    @RequestMapping("/query")
    public List<VpnPackageVO> VpnPackageQuery() {

        return vpnPackageService.VpnPackageQuery();
    }

    @Operation(summary = "单个套餐查询,按套餐id查询")
    @RequestMapping("/queryone")
    public VpnPackage VpnPackageQueryOne(@RequestBody VpnPackageRequestVO vpnpackage) {

        return vpnPackageService.VpnPackageQueryOne(vpnpackage.getId());
    }

    @Operation(summary = "编辑套餐")
    @RequestMapping("/edit")
    public Integer EditVpnPackage(@RequestBody @Valid VpnPackageRequestVO vpnpackage) {

        return vpnPackageService.EditVpnPackage(vpnpackage);
    }

    @Operation(summary = "新增套餐")
    @RequestMapping("/add")

    public VpnPackage AddVpnPackage(@RequestBody @Valid VpnPackageRequestVO vpnpackage) {
        return vpnPackageService.AddVpnPackage(vpnpackage);
    }

    @Operation(summary = "套餐状态改变,上架,下架")
    @RequestMapping("/editstatus")
    public Integer EditVpnPackageStatus(@RequestBody VpnPackageRequestVOStatus vpnpackage) {

        return vpnPackageService.EditVpnPackageStatus(vpnpackage.getId(), vpnpackage.getMempackageStatus());
    }

    @Operation(summary = "删除套餐,只需要传套餐id即可 ")
    @RequestMapping("/delete")
    public Integer DeleteVpnPackage(@RequestBody VpnPackageRequestVO vpnpackage) {
        return vpnPackageService.DeleteVpnPackage(vpnpackage.getId());

    }
}