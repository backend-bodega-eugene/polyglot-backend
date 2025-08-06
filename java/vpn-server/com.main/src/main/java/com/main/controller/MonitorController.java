package com.main.controller;

import com.main.manage.requestbodyvo.NodeParamVO;
import com.main.manage.requestbodyvo.VpnLineRequestVO;
import com.main.service.VpnLineLineService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(value = "机器管理", tags = {"这是对外接口,第三方提供的节点上班"})
@RestController
@RequestMapping(value = "/backend/thirdpartyonly", method = {RequestMethod.POST})
public class MonitorController {
    @Autowired
    VpnLineLineService vpnLineLineService;

    @Operation(summary = "添加节点")
    @RequestMapping("/add")
    public Integer AddLine(@RequestBody NodeParamVO request) {
        return vpnLineLineService.addLineByThirdparty(request);
    }

}
