package com.eugene.goalhub.match.controller;

import com.eugene.goalhub.match.service.ChampionMarketOddsService;
import dto.ChampionOddsSnapshotRequest;
import dto.ChampionOddsSnapshotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * 订单服务内部冠军赔率查询接口。
 *
 * <p>供 order-service 在冠军投注下单时查询冠军赔率快照。</p>
 */
@Tag(name = "订单内部冠军赔率接口", description = "order-service 查询冠军赔率快照")
@RestController
@RequestMapping("/internal/order/championodds")
public class InternalOrderChampionOddsController {

    /**
     * 冠军赔率管理服务。
     */
    private final ChampionMarketOddsService championMarketOddsService;

    /**
     * 创建订单服务内部冠军赔率查询接口实例。
     *
     * @param championMarketOddsService 冠军赔率管理服务
     */
    public InternalOrderChampionOddsController(
            ChampionMarketOddsService championMarketOddsService) {
        this.championMarketOddsService = championMarketOddsService;
    }

    /**
     * 查询冠军赔率快照。
     *
     * @param request 冠军赔率快照查询参数
     * @return 冠军赔率快照
     */
    @Operation(summary = "查询冠军赔率快照", description = "根据冠军赔率ID查询下单所需的赔率快照。")
    @PostMapping("/snapshot")
    public Result<ChampionOddsSnapshotResponse> snapshot(
            @Parameter(description = "冠军赔率快照查询参数", required = true)
            @RequestBody ChampionOddsSnapshotRequest request) {

        return Result.success(
                championMarketOddsService.getSnapshot(request)
        );
    }
}
