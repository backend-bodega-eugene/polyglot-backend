package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.ChampionMarketOddsService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 内部后台冠军赔率管理接口。
 *
 * <p>供 admin-service 调用，负责冠军赔率的后台管理操作。</p>
 */
@Tag(name = "内部后台冠军赔率管理", description = "内部后台冠军赔率分页查询、新增、修改和删除接口")
@RestController
@RequestMapping("/internal/admin/championmarketodds")
public class InternalAdminChampionMarketOddsController {

    /**
     * 冠军赔率管理服务。
     */
    private final ChampionMarketOddsService championMarketOddsService;

    /**
     * 创建内部后台冠军赔率管理接口实例。
     *
     * @param championMarketOddsService 冠军赔率管理服务
     */
    public InternalAdminChampionMarketOddsController(
            ChampionMarketOddsService championMarketOddsService) {
        this.championMarketOddsService = championMarketOddsService;
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
            @Valid @RequestBody ChampionMarketOddsPageRequest request) {

        return Result.success(
                championMarketOddsService.page(request)
        );
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
            @Valid @RequestBody ChampionLeagueTeamRequest request) {

        return Result.success(
                championMarketOddsService.leagueTeams(request)
        );
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
            @Valid @RequestBody AddChampionMarketOddsRequest request) {

        championMarketOddsService.add(request);

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
            @Valid @RequestBody UpdateChampionMarketOddsRequest request) {

        championMarketOddsService.update(request);

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
            @Valid @RequestBody DeleteChampionMarketOddsRequest request) {

        championMarketOddsService.delete(request);

        return Result.success();
    }
}
