package com.eugene.goalhub.order.judge.impl;

import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import com.eugene.goalhub.order.judge.BetResultJudge;
import com.eugene.goalhub.order.judge.BetSystemResult;
import dto.SaveMatchResultRequest;
import exception.BusinessException;
import org.springframework.stereotype.Component;
import response.ResultCode;

import java.util.Objects;

/**
 * 单双玩法系统预判。
 *
 * <p>根据常规时间主客队总进球数的奇偶性判断投注选项是否命中。</p>
 */
@Component
public class SingleDoubleJudge implements BetResultJudge {

    /**
     * 单双玩法编码。
     */
    private static final String PLAY_CODE =
            "MATCH_SINGLE_DOUBLE";

    /**
     * 单数选项编码。
     */
    private static final String SINGLE_VALUE=
            "SINGLE_VALUE";

    /**
     * 双数选项编码。
     */
    private static final String DOUBLE_VALUE =
            "DOUBLE_VALUE";

    /**
     * 获取支持的玩法编码。
     *
     * @return 单双玩法编码
     */
    @Override
    public String supportPlayCode() {
        return PLAY_CODE;
    }

    /**
     * 根据常规时间总进球数奇偶性判断单双结果。
     *
     * @param item   投注订单明细
     * @param result 赛事赛果
     * @return 系统预判结果
     */
    @Override
    public BetSystemResult judge(BetOrderItemEntity item, SaveMatchResultRequest result) {
        Integer homeScore = result.getRegularHomeScore();
        Integer awayScore = result.getRegularAwayScore();

        if (homeScore == null || awayScore == null) {
            throw new BusinessException(ResultCode.SCORE_CANT_NOT_NULL);
        }
        if(Objects.equals(item.getOptionCode(), SINGLE_VALUE)) {
            if ((homeScore + awayScore) % 2 != 0) {

                return BetSystemResult.WIN;
            } else {

                return BetSystemResult.LOSE;
            }
        }
        else if (Objects.equals(item.getOptionCode(), DOUBLE_VALUE)) {
            if ((homeScore + awayScore) % 2 == 0) {

                return BetSystemResult.WIN;
            } else {

                return BetSystemResult.LOSE;
            }
        }

        throw new BusinessException(ResultCode.UNSUPPORTED_GAMEPALY);
        //return null;
    }
}
