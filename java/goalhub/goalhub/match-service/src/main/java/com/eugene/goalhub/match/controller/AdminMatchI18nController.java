package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AdminLeagueI18nService;
import com.eugene.goalhub.match.service.AdminMatchI18nService;
import com.eugene.goalhub.match.service.AdminTeamI18nService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

import java.util.List;

/**
 * 内部后台赛事国际化管理接口。
 */
@Tag(name = "内部后台赛事国际化管理", description = "内部后台联赛、比赛和球队国际化配置管理接口")
@RestController
@RequestMapping("/internal/admin/matchi18n")
public class AdminMatchI18nController {

    /**
     * 后台联赛国际化服务。
     */
    private final AdminLeagueI18nService adminLeagueI18nService;

    /**
     * 后台比赛国际化服务。
     */
    private final AdminMatchI18nService adminMatchI18nService;

    /**
     * 后台球队国际化服务。
     */
    private final AdminTeamI18nService adminTeamI18nService;

    /**
     * 创建内部后台赛事国际化管理接口实例。
     *
     * @param adminLeagueI18nService 后台联赛国际化服务
     * @param adminMatchI18nService  后台比赛国际化服务
     * @param adminTeamI18nService   后台球队国际化服务
     */
    public AdminMatchI18nController(AdminLeagueI18nService adminLeagueI18nService,
                                    AdminMatchI18nService adminMatchI18nService,
                                    AdminTeamI18nService adminTeamI18nService) {
        this.adminLeagueI18nService = adminLeagueI18nService;
        this.adminMatchI18nService = adminMatchI18nService;
        this.adminTeamI18nService = adminTeamI18nService;
    }

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    @Operation(summary = "查询联赛国际化配置", description = "根据查询条件获取联赛国际化配置列表。")
    @PostMapping("/league/list")
    public Result<List<LeagueI18nResponse>> listLeagueI18n(
            @Parameter(description = "联赛国际化查询参数", required = true)
            @Valid @RequestBody LeagueI18nListRequest request) {
        return Result.success(adminLeagueI18nService.listLeagueI18n(request));
    }

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     * @return 空结果
     */
    @Operation(summary = "新增联赛国际化配置", description = "新增联赛多语言展示配置。")
    @PostMapping("/league/add")
    public Result<Void> addLeagueI18n(
            @Parameter(description = "联赛国际化新增参数", required = true)
            @Valid @RequestBody AddLeagueI18nRequest request) {
        adminLeagueI18nService.addLeagueI18n(request);
        return Result.success();
    }

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     * @return 空结果
     */
    @Operation(summary = "更新联赛国际化配置", description = "更新联赛多语言展示配置。")
    @PostMapping("/league/update")
    public Result<Void> updateLeagueI18n(
            @Parameter(description = "联赛国际化更新参数", required = true)
            @Valid @RequestBody UpdateLeagueI18nRequest request) {
        adminLeagueI18nService.updateLeagueI18n(request);
        return Result.success();
    }

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     * @return 空结果
     */
    @Operation(summary = "删除联赛国际化配置", description = "删除指定联赛多语言展示配置。")
    @PostMapping("/league/delete")
    public Result<Void> deleteLeagueI18n(
            @Parameter(description = "联赛国际化删除参数", required = true)
            @Valid @RequestBody DeleteLeagueI18nRequest request) {
        adminLeagueI18nService.deleteLeagueI18n(request);
        return Result.success();
    }

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    @Operation(summary = "查询比赛国际化配置", description = "根据查询条件获取比赛国际化配置列表。")
    @PostMapping("/match/list")
    public Result<List<MatchI18nResponse>> listMatchI18n(
            @Parameter(description = "比赛国际化查询参数", required = true)
            @Valid @RequestBody MatchI18nListRequest request) {
        return Result.success(adminMatchI18nService.listMatchI18n(request));
    }

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     * @return 空结果
     */
    @Operation(summary = "新增比赛国际化配置", description = "新增比赛多语言展示配置。")
    @PostMapping("/match/add")
    public Result<Void> addMatchI18n(
            @Parameter(description = "比赛国际化新增参数", required = true)
            @Valid @RequestBody AddMatchI18nRequest request) {
        adminMatchI18nService.addMatchI18n(request);
        return Result.success();
    }

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     * @return 空结果
     */
    @Operation(summary = "更新比赛国际化配置", description = "更新比赛多语言展示配置。")
    @PostMapping("/match/update")
    public Result<Void> updateMatchI18n(
            @Parameter(description = "比赛国际化更新参数", required = true)
            @Valid @RequestBody UpdateMatchI18nRequest request) {
        adminMatchI18nService.updateMatchI18n(request);
        return Result.success();
    }

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     * @return 空结果
     */
    @Operation(summary = "删除比赛国际化配置", description = "删除指定比赛多语言展示配置。")
    @PostMapping("/match/delete")
    public Result<Void> deleteMatchI18n(
            @Parameter(description = "比赛国际化删除参数", required = true)
            @Valid @RequestBody DeleteMatchI18nRequest request) {
        adminMatchI18nService.deleteMatchI18n(request);
        return Result.success();
    }

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    @Operation(summary = "查询球队国际化配置", description = "根据查询条件获取球队国际化配置列表。")
    @PostMapping("/team/list")
    public Result<List<TeamI18nResponse>> listTeamI18n(
            @Parameter(description = "球队国际化查询参数", required = true)
            @Valid @RequestBody TeamI18nListRequest request) {
        return Result.success(adminTeamI18nService.listTeamI18n(request));
    }

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     * @return 空结果
     */
    @Operation(summary = "新增球队国际化配置", description = "新增球队多语言展示配置。")
    @PostMapping("/team/add")
    public Result<Void> addTeamI18n(
            @Parameter(description = "球队国际化新增参数", required = true)
            @Valid @RequestBody AddTeamI18nRequest request) {
        adminTeamI18nService.addTeamI18n(request);
        return Result.success();
    }

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     * @return 空结果
     */
    @Operation(summary = "更新球队国际化配置", description = "更新球队多语言展示配置。")
    @PostMapping("/team/update")
    public Result<Void> updateTeamI18n(
            @Parameter(description = "球队国际化更新参数", required = true)
            @Valid @RequestBody UpdateTeamI18nRequest request) {
        adminTeamI18nService.updateTeamI18n(request);
        return Result.success();
    }

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     * @return 空结果
     */
    @Operation(summary = "删除球队国际化配置", description = "删除指定球队多语言展示配置。")
    @PostMapping("/team/delete")
    public Result<Void> deleteTeamI18n(
            @Parameter(description = "球队国际化删除参数", required = true)
            @Valid @RequestBody DeleteTeamI18nRequest request) {
        adminTeamI18nService.deleteTeamI18n(request);
        return Result.success();
    }
}
