package com.eugene.goalhub.match.mapper;

import dto.AppMatchOddsFlatResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AppMatchOddsMapper {

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