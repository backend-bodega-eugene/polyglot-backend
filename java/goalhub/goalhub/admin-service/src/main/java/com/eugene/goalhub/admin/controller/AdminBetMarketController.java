package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminBetMarketService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 后台投注玩法管理接口。
 *
 * <p>维护投注玩法和玩法选项配置，供后台运营管理使用。</p>
 */
@Tag(name = "后台投注玩法管理", description = "后台玩法和子玩法管理接口")
@RestController
@RequestMapping("/admin/betmarket")
public class AdminBetMarketController {

    /**
     * 后台投注玩法服务。
     */
    private final AdminBetMarketService adminBetMarketService;

    /**
     * 创建后台投注玩法管理接口实例。
     *
     * @param adminBetMarketService 后台投注玩法服务
     */
    public AdminBetMarketController(
            AdminBetMarketService adminBetMarketService) {
        this.adminBetMarketService = adminBetMarketService;
    }

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    @Operation(summary = "分页查询玩法", description = "分页查询投注玩法列表。")
    @PostMapping("/page")
    public Result<PageResponse<BetMarketResponse>> betMarketPage(
            @Parameter(description = "玩法分页查询参数", required = true)
            @RequestBody BetMarketPageRequest request) {

        return Result.success(
                adminBetMarketService.betMarketPage(request));
    }

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     * @return 空结果
     */
    @Operation(summary = "新增玩法", description = "新增投注玩法。")
    @PostMapping("/add")
    public Result<Void> addBetMarket(
            @Parameter(description = "玩法新增参数", required = true)
            @RequestBody AddBetMarketRequest request) {

        adminBetMarketService.addBetMarket(request);

        return Result.success();
    }

    /**
     * 更新投注玩法。
     *
     * @param request 投注玩法更新参数
     * @return 空结果
     */
    @Operation(summary = "更新玩法", description = "更新投注玩法。")
    @PostMapping("/update")
    public Result<Void> updateBetMarket(
            @Parameter(description = "玩法更新参数", required = true)
            @RequestBody UpdateBetMarketRequest request) {

        adminBetMarketService.updateBetMarket(request);

        return Result.success();
    }

    /**
     * 删除投注玩法。
     *
     * @param request 投注玩法删除参数
     * @return 空结果
     */
    @Operation(summary = "删除玩法", description = "删除指定投注玩法。")
    @PostMapping("/delete")
    public Result<Void> deleteBetMarket(
            @Parameter(description = "玩法删除参数", required = true)
            @RequestBody DeleteBetMarketRequest request) {

        adminBetMarketService.deleteBetMarket(request);

        return Result.success();
    }

    /**
     * 查询投注玩法选项列表。
     *
     * @param request 投注玩法选项查询条件
     * @return 投注玩法选项列表
     */
    @Operation(summary = "查询子玩法", description = "根据玩法ID查询子玩法列表。")
    @PostMapping("/option/list")
    public Result<List<BetMarketOptionResponse>> betMarketOptionList(
            @Parameter(description = "子玩法查询参数", required = true)
            @RequestBody BetMarketOptionListRequest request) {

        return Result.success(
                adminBetMarketService.betMarketOptionList(request));
    }

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     * @return 空结果
     */
    @Operation(summary = "新增子玩法", description = "新增指定玩法下的子玩法。")
    @PostMapping("/option/add")
    public Result<Void> addBetMarketOption(
            @Parameter(description = "子玩法新增参数", required = true)
            @RequestBody AddBetMarketOptionRequest request) {

        adminBetMarketService.addBetMarketOption(request);

        return Result.success();
    }

    /**
     * 更新投注玩法选项。
     *
     * @param request 投注玩法选项更新参数
     * @return 空结果
     */
    @Operation(summary = "更新子玩法", description = "更新指定子玩法。")
    @PostMapping("/option/update")
    public Result<Void> updateBetMarketOption(
            @Parameter(description = "子玩法更新参数", required = true)
            @RequestBody UpdateBetMarketOptionRequest request) {

        adminBetMarketService.updateBetMarketOption(request);

        return Result.success();
    }

    /**
     * 删除投注玩法选项。
     *
     * @param request 投注玩法选项删除参数
     * @return 空结果
     */
    @Operation(summary = "删除子玩法", description = "删除指定子玩法。")
    @PostMapping("/option/delete")
    public Result<Void> deleteBetMarketOption(
            @Parameter(description = "子玩法删除参数", required = true)
            @RequestBody DeleteBetMarketOptionRequest request) {

        adminBetMarketService.deleteBetMarketOption(request);

        return Result.success();
    }
}
