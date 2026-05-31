package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.MatchResultEntity;
import dto.AdminMatchResultPageRequest;
import dto.AdminMatchResultResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 比赛结果 Mapper。
 */
@Mapper
public interface MatchResultMapper extends BaseMapper<MatchResultEntity> {

    /**
     * 分页查询后台比赛结果列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台比赛结果分页数据
     */
    @Select("""
            <script>
            SELECT
                m.id AS matchId,
                m.match_code AS matchCode,
                mi.match_name AS matchName,

                m.home_team_id AS homeTeamId,
                hti.name AS homeTeamName,

                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,

                m.status AS matchStatus,
                m.scheduled_start_time_utc AS matchStartTime,

                r.id AS resultId,
                r.status AS resultStatus,

                r.regular_home_score AS regularHomeScore,
                r.regular_away_score AS regularAwayScore,

                r.extra_home_score AS extraHomeScore,
                r.extra_away_score AS extraAwayScore,

                r.penalty_home_score AS penaltyHomeScore,
                r.penalty_away_score AS penaltyAwayScore,

                r.match_ended_at AS matchEndedAt,

                r.home_penalty_count AS homePenaltyCount,
                r.away_penalty_count AS awayPenaltyCount,

                r.home_corner_count AS homeCornerCount,
                r.away_corner_count AS awayCornerCount,

                r.home_throw_in_count AS homeThrowInCount,
                r.away_throw_in_count AS awayThrowInCount,

                r.home_foul_count AS homeFoulCount,
                r.away_foul_count AS awayFoulCount,

                r.home_free_kick_count AS homeFreeKickCount,
                r.away_free_kick_count AS awayFreeKickCount,

                r.home_red_card_count AS homeRedCardCount,
                r.away_red_card_count AS awayRedCardCount,

                r.home_yellow_card_count AS homeYellowCardCount,
                r.away_yellow_card_count AS awayYellowCardCount

            FROM soccer_match m

            LEFT JOIN soccer_match_i18n mi
                ON mi.match_id = m.id
               AND mi.lang_code = #{req.langCode}

            LEFT JOIN soccer_team_i18n hti
                ON hti.team_id = m.home_team_id
               AND hti.lang_code = #{req.langCode}

            LEFT JOIN soccer_team_i18n ati
                ON ati.team_id = m.away_team_id
               AND ati.lang_code = #{req.langCode}

            LEFT JOIN match_results r
                ON r.match_id = m.id

            WHERE 1 = 1

            <if test="req.matchName != null and req.matchName != ''">
                AND mi.match_name LIKE CONCAT('%', #{req.matchName}, '%')
            </if>

            <if test="req.teamName != null and req.teamName != ''">
                AND (
                    hti.name LIKE CONCAT('%', #{req.teamName}, '%')
                    OR
                    ati.name LIKE CONCAT('%', #{req.teamName}, '%')
                )
            </if>

            <if test="req.matchStatus != null and req.matchStatus != ''">
                AND m.status = #{req.matchStatus}
            </if>

            <if test="req.startTime != null">
                AND m.scheduled_start_time_utc &gt;= #{req.startTime}
            </if>

            <if test="req.endTime != null">
                AND m.scheduled_start_time_utc &lt;= #{req.endTime}
            </if>

            ORDER BY
                m.scheduled_start_time_utc DESC,
                m.id DESC

            </script>
            """)
    Page<AdminMatchResultResponse> adminPage(
            Page<AdminMatchResultResponse> page,
            @Param("req") AdminMatchResultPageRequest request
    );
}
