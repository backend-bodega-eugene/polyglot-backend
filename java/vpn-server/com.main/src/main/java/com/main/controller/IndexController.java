package com.main.controller;

import com.main.manage.requestbodyvo.IndexRequestParam;
import com.main.manage.responseVO.IndexResponeVO;
import com.main.service.VpnMemberService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(value = "首页", tags = {"首页统计查询"})
@RestController
@RequestMapping(value = "/backend/index", method = {RequestMethod.POST})
public class IndexController {
    @Autowired
    VpnMemberService vpnMemberService;

    @Operation(summary = "首页查询")
    @RequestMapping("/indexquery")
    public IndexResponeVO IndexQuery(@RequestBody IndexRequestParam param) {
        return null;
    }
}