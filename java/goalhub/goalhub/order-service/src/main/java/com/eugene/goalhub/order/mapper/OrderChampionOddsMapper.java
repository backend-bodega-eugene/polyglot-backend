package com.eugene.goalhub.order.mapper;

import dto.ChampionOddsSnapshotResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单侧冠军赔率快照 Mapper。
 */
@Mapper
public interface OrderChampionOddsMapper {

    /**
     * 查询冠军赔率快照。
     *
     * @param championOddsId 冠军赔率 ID
     * @param langCode       语言编码
     * @return 冠军赔率快照
     */
    @Select("""
            SELECT
                co.id AS championOddsId,
                co.league_id AS leagueId,
                COALESCE(li.name, l.code) AS leagueName,
                co.team_id AS teamId,
                COALESCE(ti.name, co.team_name_snapshot, t.code) AS teamName,
                co.odds,
                co.visible,
                co.bet_status AS betStatus
            FROM champion_market_odds co
            INNER JOIN soccer_league l
                ON l.id = co.league_id
            INNER JOIN soccer_team t
                ON t.id = co.team_id
            LEFT JOIN soccer_league_i18n li
                ON li.league_id = co.league_id
               AND li.lang_code = #{langCode}
            LEFT JOIN soccer_team_i18n ti
                ON ti.team_id = co.team_id
               AND ti.lang_code = #{langCode}
            WHERE co.id = #{championOddsId}
            LIMIT 1
            """)
    ChampionOddsSnapshotResponse selectSnapshot(
            @Param("championOddsId") Long championOddsId,
            @Param("langCode") String langCode
    );
}
