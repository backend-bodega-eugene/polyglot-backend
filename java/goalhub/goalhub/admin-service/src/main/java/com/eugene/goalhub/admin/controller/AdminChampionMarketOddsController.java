package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminChampionMarketOddsService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 后台冠军赔率管理接口。
 *
 * <p>提供冠军市场赔率的分页查询、联赛球队查询、新增、更新和删除能力。</p>
 */
@Tag(name = "后台冠军赔率管理", description = "后台冠军赔率分页查询、新增、修改和删除接口")
@RestController
@RequestMapping("/admin/championmarketodds")
public class AdminChampionMarketOddsController {

    /**
     * 后台冠军赔率管理服务。
     */
    private final AdminChampionMarketOddsService adminChampionMarketOddsService;

    /**
     * 创建后台冠军赔率管理接口实例。
     *
     * @param adminChampionMarketOddsService 后台冠军赔率管理服务
     */
    public AdminChampionMarketOddsController(
            AdminChampionMarketOddsService adminChampionMarketOddsService) {
        this.adminChampionMarketOddsService = adminChampionMarketOddsService;
    }

    /**
     * 分页查询冠军赔率配置列表。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    @Operation(summary = "分页查询冠军赔率", description = "分页查询冠军赔率配置列表。")
    @PostMapping("/page")
    public Result<PageResponse<ChampionMarketOddsResponse>> page(
            @Parameter(description = "冠军赔率分页查询参数", required = true)
            @RequestBody ChampionMarketOddsPageRequest request) {

        return Result.success(
                adminChampionMarketOddsService.page(request));
    }

    /**
     * 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    @Operation(summary = "查询联赛下球队", description = "根据联赛ID查询该联赛下所有出现过的球队。")
    @PostMapping("/leagueteams")
    public Result<List<ChampionLeagueTeamResponse>> leagueTeams(
            @Parameter(description = "联赛球队查询参数", required = true)
            @RequestBody ChampionLeagueTeamRequest request) {

        return Result.success(
                adminChampionMarketOddsService.leagueTeams(request));
    }

    /**
     * 新增冠军赔率配置。
     *
     * @param request 新增冠军赔率参数
     * @return 空结果
     */
    @Operation(summary = "新增冠军赔率", description = "为联赛球队新增冠军赔率。")
    @PostMapping("/add")
    public Result<Void> add(
            @Parameter(description = "新增冠军赔率参数", required = true)
            @RequestBody AddChampionMarketOddsRequest request) {

        adminChampionMarketOddsService.add(request);

        return Result.success();
    }

    /**
     * 更新冠军赔率配置。
     *
     * @param request 更新冠军赔率参数
     * @return 空结果
     */
    @Operation(summary = "更新冠军赔率", description = "更新冠军赔率。")
    @PostMapping("/update")
    public Result<Void> update(
            @Parameter(description = "更新冠军赔率参数", required = true)
            @RequestBody UpdateChampionMarketOddsRequest request) {

        adminChampionMarketOddsService.update(request);

        return Result.success();
    }

    /**
     * 删除指定冠军赔率配置。
     *
     * @param request 删除冠军赔率参数
     * @return 空结果
     */
    @Operation(summary = "删除冠军赔率", description = "删除指定冠军赔率。")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Parameter(description = "删除冠军赔率参数", required = true)
            @RequestBody DeleteChampionMarketOddsRequest request) {

        adminChampionMarketOddsService.delete(request);

        return Result.success();
    }
}
