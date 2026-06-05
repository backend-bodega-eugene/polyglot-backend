package com.eugene.goalhub.match.service;

import dto.OrderMatchOptionSnapshotResponse;

/**
 * 订单赛事快照查询服务。
 *
 * <p>负责为订单服务提供下单时使用的赛事玩法赔率快照。</p>
 */
public interface OrderMatchSnapshotService {

    /**
     * 查询下单用赛事玩法赔率快照。
     *
     * @param matchMarketOptionId 赛事玩法赔率 ID
     * @return 下单用赛事玩法赔率快照
     */
    OrderMatchOptionSnapshotResponse getOrderSnapshot(
            Long matchMarketOptionId);
}
