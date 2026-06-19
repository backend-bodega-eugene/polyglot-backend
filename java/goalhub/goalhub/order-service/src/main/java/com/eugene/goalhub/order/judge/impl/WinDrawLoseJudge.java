package com.eugene.goalhub.order.judge.impl;

import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import com.eugene.goalhub.order.judge.BetResultJudge;
import com.eugene.goalhub.order.judge.BetSystemResult;
import com.eugene.goalhub.order.judge.MatchResultContext;
import dto.SaveMatchResultRequest;
import exception.BusinessException;
import org.springframework.stereotype.Component;
import response.ResultCode;

import java.util.Objects;

/**
 * 胜平负玩法系统预判。
 *
 * <p>
 * 支持选项：
 * HOME_WIN - 主胜
 * DRAW     - 平局
 * AWAY_WIN - 客胜
 * </p>
 */
@Component
public class WinDrawLoseJudge implements BetResultJudge {

    /**
     * 胜平负玩法编码。
     */
    private static final String PLAY_CODE =
            "MATCH_WIN_DRAW_LOSE";

    /**
     * 主胜选项编码。
     */
    private static final String OPTION_HOME_WIN =
            "HOME_WIN";

    /**
     * 平局选项编码。
     */
    private static final String OPTION_DRAW =
            "DRAW";

    /**
     * 客胜选项编码。
     */
    private static final String OPTION_AWAY_WIN =
            "AWAY_WIN";

    /**
     * 获取支持的玩法编码。
     *
     * @return 胜平负玩法编码
     */
    @Override
    public String supportPlayCode() {
        return PLAY_CODE;
    }

    /**
     * 根据常规时间主客队比分判断胜平负结果。
     *
     * @param item   投注订单明细
     * @param result 赛事赛果
     * @return 系统预判结果
     */
    @Override
    public BetSystemResult judge(
            BetOrderItemEntity item,
            SaveMatchResultRequest result) {

        Integer homeScore = result.getRegularHomeScore();
        Integer awayScore = result.getRegularAwayScore();

        if (homeScore == null || awayScore == null) {
            throw new BusinessException(ResultCode.SCORE_CANT_NOT_NULL);
        }

        String optionCode = item.getOptionCode();

        if (Objects.equals(optionCode, OPTION_HOME_WIN)) {

            return homeScore > awayScore
                    ? BetSystemResult.WIN
                    : BetSystemResult.LOSE;
        }

        if (Objects.equals(optionCode, OPTION_DRAW)) {

            return Objects.equals(homeScore, awayScore)
                    ? BetSystemResult.WIN
                    : BetSystemResult.LOSE;
        }

        if (Objects.equals(optionCode, OPTION_AWAY_WIN)) {

            return homeScore < awayScore
                    ? BetSystemResult.WIN
                    : BetSystemResult.LOSE;
        }

        throw new BusinessException(ResultCode.UNSUPPORTED_GAMEPALY);

    }
}
