package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.SoccerLeagueService;
import dto.SoccerLeagueResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

import java.util.List;

@RestController
@RequestMapping("/soccer/leagues")
public class SoccerLeagueController {

    private final SoccerLeagueService soccerLeagueService;

    public SoccerLeagueController(SoccerLeagueService soccerLeagueService) {
        this.soccerLeagueService = soccerLeagueService;
    }

    @GetMapping
    public Result<List<SoccerLeagueResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "langCode", required = false, defaultValue = "en-US") String langCode) {

        List<SoccerLeagueResponse> list =
                soccerLeagueService.listLeagues(keyword, langCode);

        return Result.success(list);
    }
}