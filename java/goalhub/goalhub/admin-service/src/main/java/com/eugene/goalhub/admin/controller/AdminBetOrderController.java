package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminBetOrderService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 后台投注订单管理接口。
 */
@Tag(name = "后台投注订单管理", description = "后台投注订单查询、审核、冻结和结算接口")
@RestController
@RequestMapping("/admin/order")
public class AdminBetOrderController {

    /**
     * 后台投注订单服务。
     */
    private final AdminBetOrderService adminBetOrderService;

    /**
     * 创建后台投注订单管理接口实例。
     *
     * @param adminBetOrderService 后台投注订单服务
     */
    public AdminBetOrderController(
            AdminBetOrderService adminBetOrderService) {

        this.adminBetOrderService =
                adminBetOrderService;
    }

    /**
     * 分页查询投注订单。
     *
     * @param request 投注订单分页查询条件
     * @return 投注订单分页数据
     */
    @Operation(summary = "分页查询投注订单", description = "后台分页查询投注订单主表。")
    @PostMapping("/page")
    public Result<PageResponse<AdminBetOrderResponse>> orderPage(
            @Parameter(description = "投注订单分页查询参数", required = true)
            @RequestBody AdminBetOrderPageRequest request) {

        return Result.success(
                adminBetOrderService.orderPage(request)
        );
    }

    /**
     * 分页查询历史订单。
     *
     * @param request 订单分页查询条件
     * @return 历史订单分页数据
     */
    @Operation(summary = "分页查询历史订单", description = "分页查询历史订单。")
    @PostMapping("/pagehistory")
    public Result<PageResponse<AdminBetOrderResponse>> orderPageHistory(
            @Parameter(description = "订单分页查询参数", required = true)
            @RequestBody AdminBetOrderPageRequest request) {
        request=new AdminBetOrderPageRequest();
        request.setStatus("SETTLED");
        return Result.success(
                adminBetOrderService.orderPage(request)
        );
    }

    /**
     * 分页查询投注订单明细。
     *
     * @param request 投注订单明细分页查询条件
     * @return 投注订单明细分页数据
     */
    @Operation(summary = "分页查询投注订单明细", description = "根据订单查询投注订单明细列表。")
    @PostMapping("/item/page")
    public Result<PageResponse<AdminBetOrderItemResponse>> orderItemPage(
            @Parameter(description = "投注订单明细分页查询参数", required = true)
            @RequestBody AdminBetOrderItemPageRequest request) {

        return Result.success(
                adminBetOrderService.orderItemPage(request)
        );
    }

    /**
     * 审核投注订单。
     *
     * @param adminId  当前管理员 ID
     * @param username 当前管理员用户名
     * @param request  投注订单审核参数
     * @return 空结果
     */
    @Operation(summary = "审核投注订单", description = "审核投注订单，审核结果只能由后台根据规则校验后写入。")
    @PostMapping("/review")
    public Result<Void> reviewOrder(
            @Parameter(description = "当前管理员 ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username,
            @Parameter(description = "投注订单审核参数", required = true)
            @RequestBody AdminBetOrderReviewRequest request) {
        request.setAdminId(adminId);
        request.setAdminUsername(username);

        adminBetOrderService.reviewOrder(request);

        return Result.success();
    }

    /**
     * 冻结投注订单。
     *
     * @param adminId  当前管理员 ID
     * @param username 当前管理员用户名
     * @param request  投注订单冻结参数
     * @return 空结果
     */
    @Operation(summary = "冻结投注订单", description = "冻结指定投注订单。")
    @PostMapping("/freeze")
    public Result<Void> freezeOrder(
            @Parameter(description = "当前管理员 ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username,
            @Parameter(description = "投注订单冻结参数", required = true)
            @RequestBody AdminBetOrderFreezeRequest request) {
        request.setAdminId(adminId);
        request.setAdminUsername(username);

        adminBetOrderService.freezeOrder(request);

        return Result.success();
    }

    /**
     * 结算投注订单。
     *
     * @param adminId  当前管理员 ID
     * @param username 当前管理员用户名
     * @param request  投注订单结算参数
     * @return 空结果
     */
    @Operation(summary = "结算投注订单", description = "结算投注订单，结算金额由服务端计算。")
    @PostMapping("/settle")
    public Result<Void> settleOrder(
            @Parameter(description = "当前管理员 ID", required = true)
            @RequestHeader("X-Admin-Id") Long adminId,
            @Parameter(description = "当前管理员用户名", required = true)
            @RequestHeader("X-Admin-Username") String username,
            @Parameter(description = "投注订单结算参数", required = true)
            @RequestBody AdminBetOrderSettleRequest request) {
        request.setAdminId(adminId);
        request.setAdminUsername(username);

        adminBetOrderService.settleOrder(request);

        return Result.success();
    }
}
