package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminLeagueTeamMarketOddsService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 后台联盟球队玩法赔率管理接口。
 *
 * <p>提供联盟、球队、玩法维度赔率的分页查询、新增、更新、删除和球队查询能力。</p>
 */
@Tag(name = "后台联盟球队玩法赔率管理", description = "后台联盟球队玩法赔率分页查询、新增、修改和删除接口")
@RestController
@RequestMapping("/admin/leagueteammarketodds")
public class AdminLeagueTeamMarketOddsController {

    /**
     * 后台联盟球队玩法赔率管理服务。
     */
    private final AdminLeagueTeamMarketOddsService adminLeagueTeamMarketOddsService;

    /**
     * 创建后台联盟球队玩法赔率管理接口实例。
     *
     * @param adminLeagueTeamMarketOddsService 后台联盟球队玩法赔率管理服务
     */
    public AdminLeagueTeamMarketOddsController(
            AdminLeagueTeamMarketOddsService adminLeagueTeamMarketOddsService) {

        this.adminLeagueTeamMarketOddsService =
                adminLeagueTeamMarketOddsService;
    }

    /**
     * 分页查询联盟球队玩法赔率配置列表。
     *
     * @param request 联盟球队玩法赔率分页查询参数
     * @return 联盟球队玩法赔率分页结果
     */
    @Operation(summary = "分页查询联盟球队玩法赔率", description = "分页查询联盟球队玩法赔率配置列表。")
    @PostMapping("/page")
    public Result<PageResponse<LeagueTeamMarketOddsResponse>> page(
            @Parameter(description = "联盟球队玩法赔率分页查询参数", required = true)
            @RequestBody LeagueTeamMarketOddsPageRequest request) {

        return Result.success(
                adminLeagueTeamMarketOddsService.page(request)
        );
    }

    /**
     * 新增联盟球队玩法赔率配置。
     *
     * @param request 新增联盟球队玩法赔率参数
     * @return 空结果
     */
    @Operation(summary = "新增联盟球队玩法赔率", description = "为联盟、玩法、球队新增赔率。")
    @PostMapping("/add")
    public Result<Void> add(
            @Parameter(description = "新增联盟球队玩法赔率参数", required = true)
            @RequestBody AddLeagueTeamMarketOddsRequest request) {

        adminLeagueTeamMarketOddsService.add(request);

        return Result.success();
    }

    /**
     * 更新联盟球队玩法赔率配置。
     *
     * @param request 更新联盟球队玩法赔率参数
     * @return 空结果
     */
    @Operation(summary = "更新联盟球队玩法赔率", description = "更新联盟球队玩法赔率。")
    @PostMapping("/update")
    public Result<Void> update(
            @Parameter(description = "更新联盟球队玩法赔率参数", required = true)
            @RequestBody UpdateLeagueTeamMarketOddsRequest request) {

        adminLeagueTeamMarketOddsService.update(request);

        return Result.success();
    }

    /**
     * 删除指定联盟球队玩法赔率配置。
     *
     * @param request 删除联盟球队玩法赔率参数
     * @return 空结果
     */
    @Operation(summary = "删除联盟球队玩法赔率", description = "删除指定联盟球队玩法赔率。")
    @PostMapping("/delete")
    public Result<Void> delete(
            @Parameter(description = "删除联盟球队玩法赔率参数", required = true)
            @RequestBody DeleteLeagueTeamMarketOddsRequest request) {

        adminLeagueTeamMarketOddsService.delete(request);

        return Result.success();
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
                adminLeagueTeamMarketOddsService
                        .leagueTeams(request)
        );
    }
}
