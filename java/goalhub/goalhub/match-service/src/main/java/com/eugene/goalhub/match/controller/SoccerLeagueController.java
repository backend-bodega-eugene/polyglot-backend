package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.SoccerLeagueService;
import dto.SoccerLeagueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import response.Result;

import java.util.List;

/**
 * 足球联赛查询接口。
 */
@Tag(name = "足球联赛", description = "足球联赛查询接口")
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
    @Operation(summary = "查询可用联赛列表", description = "根据关键字和语言编码查询可用足球联赛列表。")
    @GetMapping
    public Result<List<SoccerLeagueResponse>> list(
            @Parameter(description = "联赛名称、简称或编码关键字")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "语言编码", example = "en-US")
            @RequestParam(value = "langCode", required = false, defaultValue = "en-US") String langCode) {

        List<SoccerLeagueResponse> list =
                soccerLeagueService.listLeagues(keyword, langCode);

        return Result.success(list);
    }
}
