package com.eugene.goalhub.order.controller;

import com.eugene.goalhub.order.service.AdminOrderResultService;
import dto.AdminMatchResultJudgeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台订单系统预判接口。
 *
 * <p>供 admin-service 调用，根据赛事赛果生成订单明细和订单主表的系统预判结果。</p>
 */
@Tag(name = "内部后台订单系统预判", description = "根据赛事赛果生成订单系统预判结果")
@RestController
@RequestMapping("/internal/admin/orderresult")
public class InternalAdminOrderResultController {

    /**
     * 后台订单系统预判服务。
     */
    private final AdminOrderResultService adminOrderResultService;

    /**
     * 创建内部后台订单系统预判接口实例。
     *
     * @param adminOrderResultService 后台订单系统预判服务
     */
    public InternalAdminOrderResultController(
            AdminOrderResultService adminOrderResultService) {

        this.adminOrderResultService = adminOrderResultService;
    }

    /**
     * 根据赛事赛果生成订单系统预判结果。
     *
     * @param request 赛事订单系统预判请求
     * @return 空结果
     */
    @Operation(summary = "根据赛事赛果生成订单系统预判结果", description = "根据 match-service 保存的赛果生成投注订单 system_result 和预期金额。")
    @PostMapping("/match/judge")
    public Result<Void> judgeMatch(
            @Parameter(description = "赛事订单系统预判请求", required = true)
            @Valid @RequestBody AdminMatchResultJudgeRequest request) {

        adminOrderResultService.judgeMatch(request);

        return Result.success();
    }
}
