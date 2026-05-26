package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.SoccerMatchService;
import dto.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

import java.util.List;

@RestController
@RequestMapping("/soccer/matches")
public class SoccerMatchController {

    private final SoccerMatchService soccerMatchService;

    public SoccerMatchController(SoccerMatchService soccerMatchService) {
        this.soccerMatchService = soccerMatchService;
    }

    @GetMapping
    public Result<PageResponse<SoccerMatchListResponse>> page(SoccerMatchPageRequest request) {

        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageMatches(request);

        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SoccerMatchDetailResponse> detail(
            @PathVariable("id") Long id,
            SoccerMatchDetailRequest request) {

        SoccerMatchDetailResponse result =
                soccerMatchService.getMatchDetail(id, request);

        return Result.success(result);
    }
    @GetMapping("/today")
    public Result<PageResponse<SoccerMatchListResponse>> today(SoccerMatchPageRequest request) {
        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageTodayMatches(request);

        return Result.success(result);
    }

    @GetMapping("/upcoming")
    public Result<PageResponse<SoccerMatchListResponse>> upcoming(SoccerMatchPageRequest request) {
        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageUpcomingMatches(request);

        return Result.success(result);
    }

    @GetMapping("/finished")
    public Result<PageResponse<SoccerMatchListResponse>> finished(SoccerMatchPageRequest request) {
        PageResponse<SoccerMatchListResponse> result =
                soccerMatchService.pageFinishedMatches(request);

        return Result.success(result);
    }
    @GetMapping("/hot")
    public Result<List<SoccerMatchListResponse>> hot(SoccerHotMatchRequest request) {
        List<SoccerMatchListResponse> result =
                soccerMatchService.listHotMatches(request);

        return Result.success(result);
    }
}