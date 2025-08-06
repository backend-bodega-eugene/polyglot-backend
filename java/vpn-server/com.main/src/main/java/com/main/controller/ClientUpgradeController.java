package com.main.controller;

import com.main.Entity.ClientUpgrade;
import com.main.controller.base.BaseController;
import com.main.manage.responseVO.PageResultResponseVO;
import com.main.manage.requestbodyvo.ClientUpgradeQueryVO;
import com.main.service.ClientUpgradeService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "客户端更新", tags = {"客户端更新"})
@RestController
@RequestMapping(value = "/backend/clientupgrade", method = {RequestMethod.POST})
public class ClientUpgradeController extends BaseController {
    @Autowired
    ClientUpgradeService clientUpgradeService;

    @Operation(summary = "所有客户端信息查询")
    @RequestMapping("/query")
    public PageResultResponseVO<List<ClientUpgrade>> ClientUpgradeQuery(@RequestBody ClientUpgradeQueryVO vo) {
        return clientUpgradeService.ClientUpgradeServiceQuery(vo.getVersionName(), vo.getClient(), vo.getStatus(), vo.getPageIndex(), vo.getPageSize());
    }

    @Operation(summary = "添加客户端升级")
    @RequestMapping("/add")
    public Integer AddClientUpgrade(@RequestBody ClientUpgrade request) {
        return clientUpgradeService.AddClientUpgrade(request);
    }

    @Operation(summary = "修改客户端升级")
    @RequestMapping("/edit")
    public Integer EditClientUpgrade(@RequestBody ClientUpgrade request) {
        return clientUpgradeService.EditClientUpgrade(request);
    }

    @Operation(summary = "修改客户端升级状态")
    @RequestMapping("/editstatus")
    public Integer EditClientUpgradeStatus(@RequestBody ClientUpgrade request) {
        return clientUpgradeService.EditClientUpgradeStatus(request.getId(), request.getStatus());
    }

    @Operation(summary = "删除客户端升级信息")
    @RequestMapping("/delete")
    public Integer DeleteClientUpgrade(@RequestBody ClientUpgrade request) {
        return clientUpgradeService.DeleteClientUpgrade(request.getId());
    }
}
