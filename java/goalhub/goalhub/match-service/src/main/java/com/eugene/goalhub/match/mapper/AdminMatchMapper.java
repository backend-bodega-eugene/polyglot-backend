package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dto.AdminLeagueResponse;
import dto.AdminMatchResponse;
import dto.LeaguePageRequest;
import dto.MatchPageRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 后台赛事基础数据查询 Mapper。
 *
 * <p>负责后台联赛和比赛列表的多表分页查询。</p>
 */
@Mapper
public interface AdminMatchMapper {

    /**
     * 分页查询后台联赛列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台联赛分页数据
     */
    @Select("""
            <script>
            SELECT
                l.id,
                l.code,
                l.host_country AS hostCountry,
                l.logo_url AS logoUrl,
                l.status,
                i.name,
                i.short_name AS shortName,
                l.created_at AS createdAt,
                l.updated_at AS updatedAt
            FROM soccer_league l
            LEFT JOIN soccer_league_i18n i
                ON i.league_id = l.id
               AND i.lang_code = #{req.langCode}
            WHERE 1 = 1
            <if test="req.keyword != null and req.keyword != ''">
                AND (
                    i.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR i.short_name LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>
            ORDER BY l.id DESC
            </script>
            """)
    Page<AdminLeagueResponse> selectLeaguePage(
            Page<AdminLeagueResponse> page,
            @Param("req") LeaguePageRequest request
    );

    /**
     * 分页查询后台比赛列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台比赛分页数据
     */
    @Select("""
            <script>
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
                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,
                m.scheduled_start_time_utc AS scheduledStartTimeUtc,
                m.actual_start_time_utc AS actualStartTimeUtc,
                m.actual_end_time_utc AS actualEndTimeUtc,
                m.host_country AS hostCountry,
                mi.city,
                mi.venue,
                m.status,
                m.created_at AS createdAt,
                m.updated_at AS updatedAt
            FROM soccer_match m
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
            <if test="req.keyword != null and req.keyword != ''">
                AND (
                mi.match_name LIKE CONCAT('%', #{req.keyword}, '%')
                OR m.match_code LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>
            ORDER BY m.scheduled_start_time_utc DESC, m.id DESC
            </script>
            """)
    Page<AdminMatchResponse> selectMatchPage(
            Page<AdminMatchResponse> page,
            @Param("req") MatchPageRequest request
    );
}
