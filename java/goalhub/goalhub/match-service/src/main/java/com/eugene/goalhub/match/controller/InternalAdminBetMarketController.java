package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.BetMarketService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 内部后台投注玩法管理接口。
 */
@Tag(name = "内部后台投注玩法管理", description = "内部后台玩法和子玩法管理接口")
@RestController
@RequestMapping("/internal/admin/betmarket")
public class InternalAdminBetMarketController {

    /**
     * 投注玩法服务。
     */
    private final BetMarketService betMarketService;

    /**
     * 创建内部后台投注玩法管理接口实例。
     *
     * @param betMarketService 投注玩法服务
     */
    public InternalAdminBetMarketController(
            BetMarketService betMarketService) {
        this.betMarketService = betMarketService;
    }

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    @Operation(summary = "分页查询玩法", description = "分页查询投注玩法列表。")
    @PostMapping("/page")
    public Result<PageResponse<BetMarketResponse>> page(
            @Parameter(description = "玩法分页查询参数", required = true)
            @RequestBody BetMarketPageRequest request) {

        return Result.success(
                betMarketService.page(request)
        );
    }

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     * @return 空结果
     */
    @Operation(summary = "新增玩法", description = "新增投注玩法。")
    @PostMapping("/add")
    public Result<Void> add(
            @Parameter(description = "玩法新增参数", required = true)
            @RequestBody AddBetMarketRequest request) {

        betMarketService.add(request);

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
    public Result<Void> update(
            @Parameter(description = "玩法更新参数", required = true)
            @RequestBody UpdateBetMarketRequest request) {

        betMarketService.update(request);

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
    public Result<Void> delete(
            @Parameter(description = "玩法删除参数", required = true)
            @RequestBody DeleteBetMarketRequest request) {

        betMarketService.delete(request);

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
    public Result<List<BetMarketOptionResponse>> optionList(
            @Parameter(description = "子玩法查询参数", required = true)
            @RequestBody BetMarketOptionListRequest request) {

        return Result.success(
                betMarketService.optionList(request)
        );
    }

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     * @return 空结果
     */
    @Operation(summary = "新增子玩法", description = "新增指定玩法下的子玩法。")
    @PostMapping("/option/add")
    public Result<Void> addOption(
            @Parameter(description = "子玩法新增参数", required = true)
            @RequestBody AddBetMarketOptionRequest request) {

        betMarketService.addOption(request);

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
    public Result<Void> updateOption(
            @Parameter(description = "子玩法更新参数", required = true)
            @RequestBody UpdateBetMarketOptionRequest request) {

        betMarketService.updateOption(request);

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
    public Result<Void> deleteOption(
            @Parameter(description = "子玩法删除参数", required = true)
            @RequestBody DeleteBetMarketOptionRequest request) {

        betMarketService.deleteOption(request);

        return Result.success();
    }
}
