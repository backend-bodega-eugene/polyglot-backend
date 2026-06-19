package com.eugene.goalhub.order.judge;

import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import dto.SaveMatchResultRequest;
import exception.BusinessException;
import org.springframework.stereotype.Component;
import response.ResultCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投注结果系统预判路由器。
 */
@Component
public class BetResultJudgeRouter {

    /**
     * 玩法编码到系统预判器的映射。
     */
    private final Map<String, BetResultJudge> judgeMap;

    /**
     * 创建投注结果系统预判路由器。
     *
     * @param judges 系统预判器列表
     */
    public BetResultJudgeRouter(
            List<BetResultJudge> judges) {

        this.judgeMap = judges.stream()
                .collect(Collectors.toMap(
                        BetResultJudge::supportPlayCode,
                        judge -> judge
                ));
    }

    /**
     * 根据订单明细玩法编码路由到对应预判器。
     *
     * @param item    投注订单明细
     * @param context 赛事赛果
     * @return 系统预判结果
     */
    public BetSystemResult judge(
            BetOrderItemEntity item,
            SaveMatchResultRequest context) {

        BetResultJudge judge =
                judgeMap.get(item.getPlayCode());

        if (judge == null) {
            throw new BusinessException(ResultCode.UNSUPPORTED_GAMEPALY);
        }

        return judge.judge(item, context);
    }
}
