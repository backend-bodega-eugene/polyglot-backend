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
 * 大小球玩法系统预判。
 *
 * <p>根据常规时间主客队总进球数和选项编码判断大球或小球是否命中。</p>
 */
@Component
public class BigSmallJudge implements BetResultJudge {

    /**
     * 大小球玩法编码。
     */
    private static final String PLAY_CODE =
            "MATCH_BIG_SMALL";

    /**
     * 小球选项编码。
     */
    private static final String SMALL =
            "SMALL";

    /**
     * 大球选项编码。
     */
    private static final String BIG =
            "BIG";

    /**
     * 获取支持的玩法编码。
     *
     * @return 大小球玩法编码
     */
    @Override
    public String supportPlayCode() {
        return PLAY_CODE;
    }

    /**
     * 大小球进球数阈值。
     */
    private static final Integer temp=5;

    /**
     * 根据常规时间总进球数判断大小球结果。
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
        if(Objects.equals(item.getOptionCode(), SMALL)){
           if(homeScore+awayScore<=temp){

               return BetSystemResult.WIN;
           }else {
               return BetSystemResult.LOSE;
           }

        }else if(Objects.equals(item.getOptionCode(), BIG)) {

            if(homeScore+awayScore>temp){

                return BetSystemResult.WIN;
            }else {
                return BetSystemResult.LOSE;
            }
        }
        throw new BusinessException(ResultCode.UNSUPPORTED_GAMEPALY);

    }
}
