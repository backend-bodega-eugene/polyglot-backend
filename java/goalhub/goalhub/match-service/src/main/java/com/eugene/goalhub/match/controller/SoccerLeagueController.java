package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.SoccerLeagueService;
import dto.SoccerLeagueResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

import java.util.List;

/**
 * 足球联赛查询接口。
 */
@RestController
@RequestMapping("/soccer/leagues")
public class SoccerLeagueController {

    /**
     * 足球联赛服务。
     */
    private final SoccerLeagueService soccerLeagueService;

    public SoccerLeagueController(SoccerLeagueService soccerLeagueService) {
        this.soccerLeagueService = soccerLeagueService;
    }

    /**
     * 查询可用联赛列表。
     *
     * @param keyword  联赛名称、简称或编码关键字
     * @param langCode 语言编码
     * @return 联赛列表
     */
    @GetMapping
    public Result<List<SoccerLeagueResponse>> list(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "langCode", required = false, defaultValue = "en-US") String langCode) {

        List<SoccerLeagueResponse> list =
                soccerLeagueService.listLeagues(keyword, langCode);

        return Result.success(list);
    }
}
