package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 前端赛事赛果查询 Mapper。
 *
 * <p>查询已审核赛事赛果，并按前端展示结构返回分页数据。</p>
 */
@Mapper
public interface AppMatchResultMapper {

    /**
     * 分页查询前端赛事赛果。
     *
     * @param page    分页参数
     * @param request 赛事赛果查询条件
     * @return 前端赛事赛果分页数据
     */
    @Select("""
            <script>
            SELECT
                m.id AS matchId,
                m.match_code AS matchCode,
                mi.match_name AS matchName,

                m.league_id AS leagueId,
                li.name AS leagueName,

                m.home_team_id AS homeTeamId,
                hti.name AS homeTeamName,

                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,

                m.status AS matchStatus,
                m.scheduled_start_time_utc AS matchStartTime,

                r.match_ended_at AS matchEndedAt,

                r.regular_home_score AS regularHomeScore,
                r.regular_away_score AS regularAwayScore,

                r.extra_home_score AS extraHomeScore,
                r.extra_away_score AS extraAwayScore,

                r.penalty_home_score AS penaltyHomeScore,
                r.penalty_away_score AS penaltyAwayScore,

                r.home_corner_count AS homeCornerCount,
                r.away_corner_count AS awayCornerCount,

                r.home_red_card_count AS homeRedCardCount,
                r.away_red_card_count AS awayRedCardCount,

                r.home_yellow_card_count AS homeYellowCardCount,
                r.away_yellow_card_count AS awayYellowCardCount

            FROM soccer_match m

            INNER JOIN match_results r
                ON r.match_id = m.id
               AND r.status = 1

            LEFT JOIN soccer_match_i18n mi
                ON mi.match_id = m.id
               AND mi.lang_code = #{req.langCode}

            LEFT JOIN soccer_league_i18n li
                ON li.league_id = m.league_id
               AND li.lang_code = #{req.langCode}

            LEFT JOIN soccer_team_i18n hti
                ON hti.team_id = m.home_team_id
               AND hti.lang_code = #{req.langCode}

            LEFT JOIN soccer_team_i18n ati
                ON ati.team_id = m.away_team_id
               AND ati.lang_code = #{req.langCode}

            WHERE 1 = 1

            <if test="req.leagueId != null">
                AND m.league_id = #{req.leagueId}
            </if>

            <if test="req.teamName != null and req.teamName != ''">
                AND (
                    hti.name LIKE CONCAT('%', #{req.teamName}, '%')
                    OR
                    ati.name LIKE CONCAT('%', #{req.teamName}, '%')
                )
            </if>

            <if test="req.startTime != null">
                AND m.scheduled_start_time_utc &gt;= #{req.startTime}
            </if>

            <if test="req.endTime != null">
                AND m.scheduled_start_time_utc &lt;= #{req.endTime}
            </if>

            ORDER BY
                r.match_ended_at DESC,
                m.scheduled_start_time_utc DESC,
                m.id DESC
            </script>
            """)
    Page<AppMatchResultResponse> pageResult(
            Page<AppMatchResultResponse> page,
            @Param("req") AppMatchResultPageRequest request
    );
}
