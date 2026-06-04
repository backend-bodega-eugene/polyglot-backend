package com.eugene.goalhub.match.mapper;

import dto.OrderMatchOptionSnapshotResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMatchSnapshotMapper {

    @Select("""
            SELECT
                mo.id AS matchMarketOptionId,

                m.id AS matchId,
                m.match_code AS matchCode,
                mi.match_name AS matchName,
                m.status AS matchStatus,
                m.scheduled_start_time_utc AS matchStartTime,

                m.league_id AS leagueId,
                li.name AS leagueName,

                m.home_team_id AS homeTeamId,
                hti.name AS homeTeamName,

                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,

                mo.market_id AS marketId,
                mo.market_code AS marketCode,
                mo.market_name AS marketName,

                mo.market_option_id AS marketOptionId,
                mo.market_option_code AS marketOptionCode,
                mo.market_option_name AS marketOptionName,

                mo.odds,
                mo.visible,
                mo.bet_status AS betStatus

            FROM match_market_option mo

            INNER JOIN soccer_match m
                ON m.id = mo.match_id

            LEFT JOIN soccer_match_i18n mi
                ON mi.match_id = m.id
               AND mi.lang_code = #{langCode}

            LEFT JOIN soccer_league_i18n li
                ON li.league_id = m.league_id
               AND li.lang_code = #{langCode}

            LEFT JOIN soccer_team_i18n hti
                ON hti.team_id = m.home_team_id
               AND hti.lang_code = #{langCode}

            LEFT JOIN soccer_team_i18n ati
                ON ati.team_id = m.away_team_id
               AND ati.lang_code = #{langCode}

            WHERE mo.id = #{matchMarketOptionId}
            """)
    OrderMatchOptionSnapshotResponse selectOrderSnapshot(
            @Param("matchMarketOptionId") Long matchMarketOptionId,
            @Param("langCode") String langCode
    );
}