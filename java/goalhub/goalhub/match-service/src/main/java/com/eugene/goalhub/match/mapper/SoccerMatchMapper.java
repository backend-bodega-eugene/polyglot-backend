package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.SoccerMatchEntity;
import dto.SoccerHotMatchRequest;
import dto.SoccerMatchDetailResponse;
import dto.SoccerMatchListResponse;
import dto.SoccerMatchPageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SoccerMatchMapper extends BaseMapper<SoccerMatchEntity> {

    @Select("""
            <script>
            SELECT
                m.id,
                mi.match_name AS matchName,
                hti.name AS homeTeamName,
                ati.name AS awayTeamName,
                DATE_FORMAT(m.scheduled_start_time_utc, '%Y-%m-%d %H:%i:%s') AS scheduledStartTimeUtc,
                m.status
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
            WHERE 1 = 1
            <if test="req.leagueId != null">
                AND m.league_id = #{req.leagueId}
            </if>
            <if test="req.status != null and req.status != ''">
                AND m.status = #{req.status}
            </if>
            <if test="req.startTimeUtc != null and req.startTimeUtc != ''">
                AND m.scheduled_start_time_utc &gt;= #{req.startTimeUtc}
            </if>
            <if test="req.endTimeUtc != null and req.endTimeUtc != ''">
                AND m.scheduled_start_time_utc &lt;= #{req.endTimeUtc}
            </if>
            <if test="req.teamKeyword != null and req.teamKeyword != ''">
                AND (
                    hti.name LIKE CONCAT('%', #{req.teamKeyword}, '%')
                    OR ati.name LIKE CONCAT('%', #{req.teamKeyword}, '%')
                    OR hti.short_name LIKE CONCAT('%', #{req.teamKeyword}, '%')
                    OR ati.short_name LIKE CONCAT('%', #{req.teamKeyword}, '%')
                )
            </if>
            <if test="req.keyword != null and req.keyword != ''">
                AND (
                    mi.match_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mi.stage_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mi.city LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mi.venue LIKE CONCAT('%', #{req.keyword}, '%')
                    OR hti.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR ati.name LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>
            ORDER BY m.scheduled_start_time_utc ASC, m.id ASC
            </script>
            """)
    Page<SoccerMatchListResponse> selectMatchPage(
            Page<SoccerMatchListResponse> page,
            @Param("req") SoccerMatchPageRequest request
    );

    @Select("""
            SELECT
                m.id,
                m.league_id AS leagueId,
                li.name AS leagueName,
                m.match_code AS matchCode,
                mi.match_name AS matchName,
                m.stage_code AS stageCode,
                mi.stage_name AS stageName,
                m.home_team_id AS homeTeamId,
                hti.name AS homeTeamName,
                hti.short_name AS homeTeamShortName,
                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,
                ati.short_name AS awayTeamShortName,
                DATE_FORMAT(m.scheduled_start_time_utc, '%Y-%m-%d %H:%i:%s') AS scheduledStartTimeUtc,
                DATE_FORMAT(m.actual_start_time_utc, '%Y-%m-%d %H:%i:%s') AS actualStartTimeUtc,
                DATE_FORMAT(m.actual_end_time_utc, '%Y-%m-%d %H:%i:%s') AS actualEndTimeUtc,
                m.host_country AS hostCountry,
                mi.city,
                mi.venue,
                m.status
            FROM soccer_match m
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
            WHERE m.id = #{id}
            LIMIT 1
            """)
    SoccerMatchDetailResponse selectMatchDetail(
            @Param("id") Long id,
            @Param("langCode") String langCode
    );
    @Select("""
        SELECT
            m.id,
            mi.match_name AS matchName,
            hti.name AS homeTeamName,
            ati.name AS awayTeamName,
            DATE_FORMAT(m.scheduled_start_time_utc, '%Y-%m-%d %H:%i:%s') AS scheduledStartTimeUtc,
            m.status
        FROM user_match_follow f
        INNER JOIN soccer_match m
            ON m.id = f.match_id
        LEFT JOIN soccer_match_i18n mi
            ON mi.match_id = m.id
           AND mi.lang_code = #{req.langCode}
        LEFT JOIN soccer_team_i18n hti
            ON hti.team_id = m.home_team_id
           AND hti.lang_code = #{req.langCode}
        LEFT JOIN soccer_team_i18n ati
            ON ati.team_id = m.away_team_id
           AND ati.lang_code = #{req.langCode}
        GROUP BY
            m.id,
            mi.match_name,
            hti.name,
            ati.name,
            m.scheduled_start_time_utc,
            m.status
        ORDER BY COUNT(f.match_id) DESC,
                 m.scheduled_start_time_utc ASC
        LIMIT ${req.limit}
        """)
    List<SoccerMatchListResponse> selectHotMatches(
            @Param("req") SoccerHotMatchRequest request
    );
}