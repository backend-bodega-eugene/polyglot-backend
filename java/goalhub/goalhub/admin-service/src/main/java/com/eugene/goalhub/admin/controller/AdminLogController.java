package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import com.eugene.goalhub.boot.logs.service.GoalhubLogQueryService;
import dto.LogQueryRequest;
import dto.PageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

@RestController
@RequestMapping("/admin/logs")
public class AdminLogController {

    private final GoalhubLogQueryService goalhubLogQueryService;

    public AdminLogController(GoalhubLogQueryService goalhubLogQueryService) {
        this.goalhubLogQueryService = goalhubLogQueryService;
    }

    @PostMapping("/biz/page")
    public Result<PageResponse<BizLogDocument>> queryBizLogs(@RequestBody LogQueryRequest request) {
        return Result.success(goalhubLogQueryService.queryBizLogs(request));
    }

    @PostMapping("/sys/page")
    public Result<PageResponse<SysLogDocument>> querySysLogs(@RequestBody LogQueryRequest request) {
        return Result.success(goalhubLogQueryService.querySysLogs(request));
    }

    @PostMapping("/err/page")
    public Result<PageResponse<ErrLogDocument>> queryErrLogs(@RequestBody LogQueryRequest request) {
        return Result.success(goalhubLogQueryService.queryErrLogs(request));
    }
}