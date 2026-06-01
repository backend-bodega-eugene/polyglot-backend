package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.SoccerMatchService;
import dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

import java.util.List;

/**
 * 足球比赛查询接口。
 */
@Tag(name = "足球比赛", description = "足球比赛分页、详情、今日、即将开始、已结束和热门比赛查询接口")
@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchController {

    /**
     * 足球比赛服务。
     */
    private final SoccerMatchService soccerMatchService;

    /**
     * 创建足球比赛查询接口实例。
     *
     * @param soccerMatchService 足球比赛服务
     */
    public SoccerMatchController(SoccerMatchService soccerMatchService) {
        this.soccerMatchService = soccerMatchService;
    }

    /**
     * 分页查询足球比赛。
     *
     * @param request 分页和筛选条件
     * @return 足球比赛分页结果
     */
    @Operation(summary = "分页查询足球比赛", description = "根据分页参数和筛选条件查询足球比赛列表。")
    @GetMapping
    public Result<PageResponse<SoccerMatchListResponse>> page(@ParameterObject SoccerMatchPageRequest request) {

        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageMatches(request);

        return Result.success(result);
    }

    /**
     * 查询足球比赛详情。
     *
     * @param id      赛事 ID
     * @param request 详情查询参数
     * @return 足球比赛详情
     */
    @Operation(summary = "查询足球比赛详情", description = "根据赛事 ID 和语言编码查询足球比赛详情。")
    @GetMapping("/{id}")
    public Result<SoccerMatchDetailResponse> detail(
            @Parameter(description = "赛事 ID", required = true)
            @PathVariable("id") Long id,
            @ParameterObject
            SoccerMatchDetailRequest request) {

        SoccerMatchDetailResponse result =
                soccerMatchService.getMatchDetail(id, request);

        return Result.success(result);
    }

    /**
     * 分页查询今日比赛。
     *
     * @param request 分页和筛选条件
     * @return 今日比赛分页结果
     */
    @Operation(summary = "分页查询今日比赛", description = "根据分页参数和筛选条件查询今日足球比赛列表。")
    @GetMapping("/today")
    public Result<PageResponse<SoccerMatchListResponse>> today(@ParameterObject SoccerMatchPageRequest request) {
        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageTodayMatches(request);

        return Result.success(result);
    }

    /**
     * 分页查询即将开始的比赛。
     *
     * @param request 分页和筛选条件
     * @return 即将开始比赛分页结果
     */
    @Operation(summary = "分页查询即将开始比赛", description = "根据分页参数和筛选条件查询即将开始的足球比赛列表。")
    @GetMapping("/upcoming")
    public Result<PageResponse<SoccerMatchListResponse>> upcoming(@ParameterObject SoccerMatchPageRequest request) {
        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageUpcomingMatches(request);

        return Result.success(result);
    }

    /**
     * 分页查询已结束比赛。
     *
     * @param request 分页和筛选条件
     * @return 已结束比赛分页结果
     */
    @Operation(summary = "分页查询已结束比赛", description = "根据分页参数和筛选条件查询已结束的足球比赛列表。")
    @GetMapping("/finished")
    public Result<PageResponse<SoccerMatchListResponse>> finished(@ParameterObject SoccerMatchPageRequest request) {
        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageFinishedMatches(request);

        return Result.success(result);
    }

    /**
     * 查询热门比赛。
     *
     * @param request 热门比赛查询参数
     * @return 热门比赛列表
     */
    @Operation(summary = "查询热门比赛", description = "根据语言编码和返回数量查询热门足球比赛列表。")
    @GetMapping("/hot")
    public Result<List<SoccerMatchListResponse>> hot(@ParameterObject SoccerHotMatchRequest request) {
        List<SoccerMatchListResponse> result =
                soccerMatchService.listHotMatches(request);

        return Result.success(result);
    }
}
