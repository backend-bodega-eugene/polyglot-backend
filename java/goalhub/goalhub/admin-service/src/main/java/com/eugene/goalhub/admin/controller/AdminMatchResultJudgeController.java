package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMatchResultJudgeService;
import dto.SaveMatchResultRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台赛事赛果与订单系统预判接口。
 *
 * <p>
 * 用于后台设置赛事赛果，并触发 order-service 生成对应订单 system_result。
 * 此接口不会派奖、不会写账变、不会修改用户余额。
 * </p>
 */
@Tag(name = "后台赛事赛果预判", description = "后台设置赛果并触发订单系统预判")
@RestController
@RequestMapping("/admin/matchresult/judge")
public class AdminMatchResultJudgeController {

    /**
     * 后台赛事赛果与订单系统预判编排服务。
     */
    private final AdminMatchResultJudgeService adminMatchResultJudgeService;

    /**
     * 创建后台赛事赛果与订单系统预判接口实例。
     *
     * @param adminMatchResultJudgeService 后台赛事赛果与订单系统预判编排服务
     */
    public AdminMatchResultJudgeController(
            AdminMatchResultJudgeService adminMatchResultJudgeService) {

        this.adminMatchResultJudgeService =
                adminMatchResultJudgeService;
    }

    /**
     * 设置赛果并生成订单系统预判结果。
     *
     * @param adminId  当前管理员 ID
     * @param username 当前管理员用户名
     * @param request  赛果保存参数
     * @return 空结果
     */
    @Operation(summary = "设置赛果并生成订单系统预判", description = "先保存赛事赛果，再生成订单 system_result 和预期金额。")
    @PostMapping("/save")
    public Result<Void> saveResultAndJudgeOrders(
            @Parameter(description = "当前管理员 ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username,
            @Parameter(description = "赛果保存参数", required = true)
            @RequestBody SaveMatchResultRequest request) {

        adminMatchResultJudgeService.saveResultAndJudgeOrders(
                request,
                adminId,
                username
        );

        return Result.success();
    }
}