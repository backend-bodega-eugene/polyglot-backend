package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.AppChampionOddsService;
import dto.AppChampionOddsPageRequest;
import dto.AppChampionOddsResponse;
import dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 前端冠军赔率查询接口。
 *
 * <p>提供 App 端按联赛分页查询冠军赔率的能力。</p>
 */
@Tag(name = "前端冠军赔率接口", description = "前端App冠军赔率查询接口")
@RestController
@RequestMapping("/soccer/champion")
public class SoccerChampionOddsController {

    /**
     * App 冠军赔率查询服务。
     */
    private final AppChampionOddsService appChampionOddsService;

    /**
     * 创建前端冠军赔率查询接口实例。
     *
     * @param appChampionOddsService App 冠军赔率查询服务
     */
    public SoccerChampionOddsController(
            AppChampionOddsService appChampionOddsService) {
        this.appChampionOddsService = appChampionOddsService;
    }

    /**
     * 分页查询前端冠军赔率。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    @Operation(summary = "分页查询冠军赔率", description = "按联赛查询冠军球队赔率。")
    @PostMapping("/page")
    public Result<PageResponse<AppChampionOddsResponse>> page(
            @Parameter(description = "冠军赔率分页查询参数", required = true)
            @Valid @RequestBody AppChampionOddsPageRequest request) {

        return Result.success(
                appChampionOddsService.pageChampionOdds(request)
        );
    }
}
