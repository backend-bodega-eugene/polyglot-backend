package com.eugene.goalhub.order.service;

import dto.ChampionOddsSnapshotRequest;
import dto.ChampionOddsSnapshotResponse;

/**
 * 订单侧冠军赔率快照查询服务。
 */
public interface OrderChampionOddsService {

    /**
     * 查询冠军赔率快照。
     *
     * @param request 冠军赔率快照查询参数
     * @return 冠军赔率快照
     */
    ChampionOddsSnapshotResponse getSnapshot(
            ChampionOddsSnapshotRequest request);
}
