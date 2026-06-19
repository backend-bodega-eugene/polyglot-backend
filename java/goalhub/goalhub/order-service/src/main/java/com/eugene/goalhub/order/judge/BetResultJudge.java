package com.eugene.goalhub.order.judge;

import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import dto.SaveMatchResultRequest;

/**
 * 投注结果系统预判接口。
 */
public interface BetResultJudge {

    /**
     * 支持的玩法编码。
     *
     * @return 玩法编码
     */
    String supportPlayCode();

    /**
     * 根据订单明细快照和赛事结果判断系统结果。
     *
     * @param item   投注订单明细快照
     * @param result 赛事赛果
     * @return 系统预判结果
     */
    BetSystemResult judge(
            BetOrderItemEntity item,
            SaveMatchResultRequest result);
}
