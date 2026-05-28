package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.SoccerMatchService;
import dto.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

import java.util.List;

/**
 * 足球比赛查询接口。
 */
@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchController {

    /**
     * 足球比赛服务。
     */
    private final SoccerMatchService soccerMatchService;

    public SoccerMatchController(SoccerMatchService soccerMatchService) {
        this.soccerMatchService = soccerMatchService;
    }

    /**
     * 分页查询足球比赛。
     *
     * @param request 分页和筛选条件
     * @return 足球比赛分页结果
     */
    @GetMapping
    public Result<PageResponse<SoccerMatchListResponse>> page(SoccerMatchPageRequest request) {

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
    @GetMapping("/{id}")
    public Result<SoccerMatchDetailResponse> detail(
            @PathVariable("id") Long id,
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
    @GetMapping("/today")
    public Result<PageResponse<SoccerMatchListResponse>> today(SoccerMatchPageRequest request) {
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
    @GetMapping("/upcoming")
    public Result<PageResponse<SoccerMatchListResponse>> upcoming(SoccerMatchPageRequest request) {
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
    @GetMapping("/finished")
    public Result<PageResponse<SoccerMatchListResponse>> finished(SoccerMatchPageRequest request) {
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
    @GetMapping("/hot")
    public Result<List<SoccerMatchListResponse>> hot(SoccerHotMatchRequest request) {
        List<SoccerMatchListResponse> result =
                soccerMatchService.listHotMatches(request);

        return Result.success(result);
    }
}
