package com.main.controller;

import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Api(value = "日志查询", tags = {"日志查询"})
@RestController
@RequestMapping(value = "/backend/businesslog", method = {RequestMethod.POST})
public class BusiniseeLogController {
    @Operation(summary = "日志查询")
    @RequestMapping("/query")
    public void BusinessLogQuery() {

    }
}
