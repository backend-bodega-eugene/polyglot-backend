package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AdminMatchService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 内部后台赛事基础数据管理接口。
 */
@Tag(name = "内部后台赛事管理", description = "内部后台联赛、比赛和球队管理接口")
@RestController
@RequestMapping("/internal/admin")
public class AdminMatchController {

    /**
     * 后台赛事基础数据服务。
     */
    private final AdminMatchService adminMatchService;

    /**
     * 创建内部后台赛事基础数据管理接口实例。
     *
     * @param adminMatchService 后台赛事基础数据服务
     */
    public AdminMatchController(AdminMatchService adminMatchService) {
        this.adminMatchService = adminMatchService;
    }

    /**
     * 分页查询联赛。
     *
     * @param request 联赛分页查询条件
     * @return 联赛分页数据
     */
    @Operation(summary = "分页查询联赛", description = "根据分页条件和筛选条件查询联赛列表。")
    @PostMapping("/league/page")
    public Result<PageResponse<AdminLeagueResponse>> leaguePage(
            @Parameter(description = "联赛分页查询参数", required = true)
            @Valid @RequestBody LeaguePageRequest request) {

        return Result.success(
                adminMatchService.leaguePage(request)
        );
    }

    /**
     * 新增联赛。
     *
     * @param request 联赛新增参数
     * @return 空结果
     */
    @Operation(summary = "新增联赛", description = "新增联赛基础信息。")
    @PostMapping("/league/add")
    public Result<Void> addLeague(
            @Parameter(description = "联赛新增参数", required = true)
            @Valid @RequestBody AddLeagueRequest request) {

        adminMatchService.addLeague(request);

        return Result.success();
    }

    /**
     * 更新联赛。
     *
     * @param request 联赛更新参数
     * @return 空结果
     */
    @Operation(summary = "更新联赛", description = "更新联赛基础信息。")
    @PostMapping("/league/update")
    public Result<Void> updateLeague(
            @Parameter(description = "联赛更新参数", required = true)
            @Valid @RequestBody UpdateLeagueRequest request) {

        adminMatchService.updateLeague(request);

        return Result.success();
    }

    /**
     * 删除联赛。
     *
     * @param request 联赛删除参数
     * @return 空结果
     */
    @Operation(summary = "删除联赛", description = "删除指定联赛。")
    @PostMapping("/league/delete")
    public Result<Void> deleteLeague(
            @Parameter(description = "联赛删除参数", required = true)
            @Valid @RequestBody DeleteLeagueRequest request) {

        adminMatchService.deleteLeague(request);

        return Result.success();
    }

    /**
     * 分页查询比赛。
     *
     * @param request 比赛分页查询条件
     * @return 比赛分页数据
     */
    @Operation(summary = "分页查询比赛", description = "根据分页条件和筛选条件查询比赛列表。")
    @PostMapping("/match/page")
    public Result<PageResponse<AdminMatchResponse>> matchPage(
            @Parameter(description = "比赛分页查询参数", required = true)
            @Valid @RequestBody MatchPageRequest request) {

        return Result.success(
                adminMatchService.matchPage(request)
        );
    }

    /**
     * 新增比赛。
     *
     * @param request 比赛新增参数
     * @return 空结果
     */
    @Operation(summary = "新增比赛", description = "新增比赛基础信息。")
    @PostMapping("/match/add")
    public Result<Void> addMatch(
            @Parameter(description = "比赛新增参数", required = true)
            @Valid @RequestBody AddMatchRequest request) {

        adminMatchService.addMatch(request);

        return Result.success();
    }

    /**
     * 更新比赛。
     *
     * @param request 比赛更新参数
     * @return 空结果
     */
    @Operation(summary = "更新比赛", description = "更新比赛基础信息。")
    @PostMapping("/match/update")
    public Result<Void> updateMatch(
            @Parameter(description = "比赛更新参数", required = true)
            @Valid @RequestBody UpdateMatchRequest request) {

        adminMatchService.updateMatch(request);

        return Result.success();
    }

    /**
     * 删除比赛。
     *
     * @param request 比赛删除参数
     * @return 空结果
     */
    @Operation(summary = "删除比赛", description = "删除指定比赛。")
    @PostMapping("/match/delete")
    public Result<Void> deleteMatch(
            @Parameter(description = "比赛删除参数", required = true)
            @Valid @RequestBody DeleteMatchRequest request) {

        adminMatchService.deleteMatch(request);

        return Result.success();
    }

    /**
     * 分页查询球队。
     *
     * @param request 球队分页查询条件
     * @return 球队分页数据
     */
    @Operation(summary = "分页查询球队", description = "根据分页条件和筛选条件查询球队列表。")
    @PostMapping("/match/team/page")
    public Result<PageResponse<AdminTeamResponse>> teamPage(
            @Parameter(description = "球队分页查询参数", required = true)
            @Valid @RequestBody TeamPageRequest request) {

        return Result.success(adminMatchService.teamPage(request));
    }

    /**
     * 新增球队。
     *
     * @param request 球队新增参数
     * @return 空结果
     */
    @Operation(summary = "新增球队", description = "新增球队基础信息。")
    @PostMapping("/match/team/add")
    public Result<Void> addTeam(
            @Parameter(description = "球队新增参数", required = true)
            @Valid @RequestBody AddTeamRequest request) {

        adminMatchService.addTeam(request);

        return Result.success();
    }

    /**
     * 更新球队。
     *
     * @param request 球队更新参数
     * @return 空结果
     */
    @Operation(summary = "更新球队", description = "更新球队基础信息。")
    @PostMapping("/match/team/update")
    public Result<Void> updateTeam(
            @Parameter(description = "球队更新参数", required = true)
            @Valid @RequestBody UpdateTeamRequest request) {

        adminMatchService.updateTeam(request);

        return Result.success();
    }

    /**
     * 删除球队。
     *
     * @param request 球队删除参数
     * @return 空结果
     */
    @Operation(summary = "删除球队", description = "删除指定球队。")
    @PostMapping("/match/team/delete")
    public Result<Void> deleteTeam(
            @Parameter(description = "球队删除参数", required = true)
            @Valid @RequestBody DeleteTeamRequest request) {

        adminMatchService.deleteTeam(request);

        return Result.success();
    }
}
