package com.eugene.goalhub.admin.controller;

import com.eugene.goalhub.admin.service.AdminMatchI18nService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;
import java.util.List;

/**
 * 后台赛事国际化管理接口。
 */
@Tag(name = "后台赛事国际化管理", description = "后台联赛、比赛和球队国际化配置管理接口")
@RestController
@RequestMapping("/admin/matchi18n")
public class AdminMatchI18nController {

    private final AdminMatchI18nService adminMatchI18nService;

    public AdminMatchI18nController(AdminMatchI18nService adminMatchI18nService) {
        this.adminMatchI18nService = adminMatchI18nService;
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
            @RequestBody LeagueI18nListRequest request) {

        return Result.success(adminMatchI18nService.listLeagueI18n(request));
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
            @RequestBody AddLeagueI18nRequest request) {

        adminMatchI18nService.addLeagueI18n(request);
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
            @RequestBody UpdateLeagueI18nRequest request) {

        adminMatchI18nService.updateLeagueI18n(request);
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
            @RequestBody DeleteLeagueI18nRequest request) {

        adminMatchI18nService.deleteLeagueI18n(request);
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
            @RequestBody MatchI18nListRequest request) {

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
            @RequestBody AddMatchI18nRequest request) {

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
            @RequestBody UpdateMatchI18nRequest request) {

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
            @RequestBody DeleteMatchI18nRequest request) {

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
            @RequestBody TeamI18nListRequest request) {

        return Result.success(adminMatchI18nService.listTeamI18n(request));
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
            @RequestBody AddTeamI18nRequest request) {

        adminMatchI18nService.addTeamI18n(request);
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
            @RequestBody UpdateTeamI18nRequest request) {

        adminMatchI18nService.updateTeamI18n(request);
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
            @RequestBody DeleteTeamI18nRequest request) {

        adminMatchI18nService.deleteTeamI18n(request);
        return Result.success();
    }
}
