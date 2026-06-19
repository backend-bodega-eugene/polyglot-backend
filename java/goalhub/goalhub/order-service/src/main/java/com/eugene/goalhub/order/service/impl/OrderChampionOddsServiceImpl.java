package com.eugene.goalhub.order.service.impl;

import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import com.eugene.goalhub.order.mapper.OrderChampionOddsMapper;
import com.eugene.goalhub.order.service.OrderChampionOddsService;
import dto.ChampionOddsSnapshotRequest;
import dto.ChampionOddsSnapshotResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

/**
 * 订单侧冠军赔率快照查询服务实现。
 *
 * <p>按冠军赔率 ID 查询下单所需的联赛、球队、赔率和投注状态快照。</p>
 */
@Service
public class OrderChampionOddsServiceImpl implements OrderChampionOddsService {

    /**
     * 日志模块名称。
     */
    private static final String MODULE_NAME = "订单冠军赔率快照";

    /**
     * 默认语言编码。
     */
    private static final String DEFAULT_LANG_CODE = "zh-CN";

    /**
     * 订单侧冠军赔率快照 Mapper。
     */
    private final OrderChampionOddsMapper orderChampionOddsMapper;

    /**
     * 日志写入服务。
     */
    private final GoalhubLogService goalhubLogService;

    /**
     * 创建订单侧冠军赔率快照查询服务实现。
     *
     * @param orderChampionOddsMapper 订单侧冠军赔率快照 Mapper
     * @param goalhubLogService       日志写入服务
     */
    public OrderChampionOddsServiceImpl(
            OrderChampionOddsMapper orderChampionOddsMapper,
            GoalhubLogService goalhubLogService) {
        this.orderChampionOddsMapper = orderChampionOddsMapper;
        this.goalhubLogService = goalhubLogService;
    }

    /**
     * 查询冠军赔率快照。
     *
     * @param request 冠军赔率快照查询参数
     * @return 冠军赔率快照
     */
    @Override
    public ChampionOddsSnapshotResponse getSnapshot(
            ChampionOddsSnapshotRequest request) {

        if (request == null || request.getChampionOddsId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String langCode = request.getLangCode();
        if (langCode == null || langCode.isBlank()) {
            langCode = DEFAULT_LANG_CODE;
        }

        ChampionOddsSnapshotResponse snapshot =
                orderChampionOddsMapper.selectSnapshot(
                        request.getChampionOddsId(),
                        langCode
                );

        if (snapshot == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        goalhubLogService.sysLog(
                MODULE_NAME,
                "GET_CHAMPION_ODDS_SNAPSHOT",
                "查询冠军赔率快照成功，championOddsId=" + request.getChampionOddsId()
        );

        return snapshot;
    }
}
