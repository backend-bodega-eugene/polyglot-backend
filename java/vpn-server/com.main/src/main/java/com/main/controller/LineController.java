package com.main.controller;

import com.main.Entity.VpnLine;
import com.main.manage.requestbodyvo.EditLineStatusVO;
import com.main.manage.requestbodyvo.PageResultRequestVOByLine;
import com.main.manage.requestbodyvo.VpnLineRequestVO;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.service.VpnLineLineService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "节点管理", tags = {"节点查询,修改,增加,修改状态,删除"})
@RestController
@RequestMapping(value = "/backend/line", method = {RequestMethod.POST})
public class LineController {
    @Autowired
    VpnLineLineService vpnLineLineService;

    @Operation(summary = "节点查询")
    @RequestMapping("/query")
    public PageResultResponseVO<List<VpnLine>> LineQuery(@RequestBody PageResultRequestVOByLine query) {
        return vpnLineLineService.LineQuery(query);
    }

    @Operation(summary = "添加节点")
    @RequestMapping("/add")
    public Integer AddLine(@RequestBody VpnLineRequestVO request) {
        return vpnLineLineService.addLine(request);
    }

    @Operation(summary = "修改节点")
    @RequestMapping("/edit")
    public Integer EditLine(@RequestBody VpnLineRequestVO request) {
        return vpnLineLineService.EditLine(request);
    }

    @Operation(summary = "修改节点状态")
    @RequestMapping("/editstatus")
    public Integer EditLineStatus(@RequestBody EditLineStatusVO vo) {
        return vpnLineLineService.EditLineStatus(vo.getLineId(), vo.getStatus());
    }

    @Operation(summary = "删除节点 传参忽略状态字段")
    @RequestMapping("/delete")
    public Integer DeleteLine(@RequestBody EditLineStatusVO vo) {
        return vpnLineLineService.DeleteLine(vo.getLineId());
    }
}
