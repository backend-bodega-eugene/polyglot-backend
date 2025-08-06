package com.app.controller;

import com.app.controller.base.BaseController;
import com.app.controller.dto.IdDTOVO;
import com.app.controller.dto.VpnLineResponseParam;
import com.app.controller.dto.VpnVmessVO;
import com.app.service.VpnLineLineService;
import com.app.service.VpnNationService;
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
@Api(value = "节点", tags = {"所有节点查询"})
@RestController
@RequestMapping(value = "/app/line", method = {RequestMethod.POST})
public class VpnLineController extends BaseController {

    @Autowired
    VpnLineLineService vpnLineLineService;
    @Autowired
    VpnNationService nationService;

    @Operation(summary = "节点查询")
    @RequestMapping("/query")
    public List<VpnLineResponseParam> LineQuery() {
        return vpnLineLineService.LineQuery(getSession());
    }

    @Operation(summary = "连接节点")
    @RequestMapping("/connectnode")
    public VpnVmessVO connectNode(@RequestBody IdDTOVO dto) {
        return vpnLineLineService.connectNode(dto.getId(), getSession());
    }

}

