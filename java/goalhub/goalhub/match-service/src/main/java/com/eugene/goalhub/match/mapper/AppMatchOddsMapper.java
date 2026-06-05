package com.eugene.goalhub.match.mapper;

import dto.AppMatchOddsFlatResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 前端赛事赔率查询 Mapper。
 *
 * <p>查询前端可见且开放投注的赛事玩法赔率。</p>
 */
@Mapper
public interface AppMatchOddsMapper {

    /**
     * 查询指定赛事下可展示的赔率列表。
     *
     * @param matchId 赛事 ID
     * @return 赛事赔率平铺列表
     */
    @Select("""
            SELECT
                mo.id,
                mo.match_id AS matchId,

                mo.market_id AS marketId,
                mo.market_code AS marketCode,
                mo.market_name AS marketName,

                mo.market_option_id AS marketOptionId,
                mo.market_option_code AS marketOptionCode,
                mo.market_option_name AS marketOptionName,

                mo.odds,
                mo.bet_status AS betStatus,
                mo.sort_order AS sortOrder

            FROM match_market_option mo

            WHERE mo.match_id = #{matchId}
              AND mo.visible = 1
              AND mo.bet_status = 'OPEN'

            ORDER BY
                mo.market_id ASC,
                mo.sort_order ASC,
                mo.id ASC
            """)
    List<AppMatchOddsFlatResponse> listByMatchId(
            @Param("matchId") Long matchId
    );
}
