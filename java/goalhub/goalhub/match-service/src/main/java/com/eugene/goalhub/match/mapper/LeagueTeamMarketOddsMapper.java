package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.LeagueTeamMarketOddsEntity;
import dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 联盟球队玩法赔率 Mapper。
 */
@Mapper
public interface LeagueTeamMarketOddsMapper
        extends BaseMapper<LeagueTeamMarketOddsEntity> {

    /**
     * 后台分页查询联盟球队玩法赔率配置。
     *
     * @param page    MyBatis-Plus 分页参数
     * @param request 联盟球队玩法赔率分页查询参数
     * @return 联盟球队玩法赔率分页数据
     */
    @Select("""
            <script>
            SELECT
                o.id,
                o.league_id AS leagueId,
                li.name AS leagueName,

                o.play_id AS playId,
                o.play_code AS playCode,
                o.play_name AS playName,

                o.team_id AS teamId,
                ti.name AS teamName,
                o.team_name_snapshot AS teamNameSnapshot,

                o.odds,
                o.visible,
                o.bet_status AS betStatus,
                o.sort_order AS sortOrder,
                o.created_at AS createdAt,
                o.updated_at AS updatedAt
            FROM league_team_market_odds o

            LEFT JOIN soccer_league_i18n li
                ON li.league_id = o.league_id
               AND li.lang_code = #{req.langCode}

            LEFT JOIN soccer_team_i18n ti
                ON ti.team_id = o.team_id
               AND ti.lang_code = #{req.langCode}

            WHERE 1 = 1

            <if test="req.leagueId != null">
                AND o.league_id = #{req.leagueId}
            </if>

            <if test="req.playId != null">
                AND o.play_id = #{req.playId}
            </if>

            <if test="req.playCode != null and req.playCode != ''">
                AND o.play_code = #{req.playCode}
            </if>

            <if test="req.teamId != null">
                AND o.team_id = #{req.teamId}
            </if>

            <if test="req.visible != null">
                AND o.visible = #{req.visible}
            </if>

            <if test="req.betStatus != null and req.betStatus != ''">
                AND o.bet_status = #{req.betStatus}
            </if>

            <if test="req.keyword != null and req.keyword != ''">
                AND (
                    li.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR ti.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR o.play_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR o.play_code LIKE CONCAT('%', #{req.keyword}, '%')
                    OR o.team_name_snapshot LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>

            ORDER BY
                o.league_id DESC,
                o.play_id ASC,
                o.sort_order ASC,
                o.id DESC
            </script>
            """)
    Page<LeagueTeamMarketOddsResponse> adminPage(
            Page<LeagueTeamMarketOddsResponse> page,
            @Param("req") LeagueTeamMarketOddsPageRequest request
    );

    /**
     * 查询指定联赛下出现过的球队。
     *
     * @param leagueId 联赛或杯赛 ID
     * @param langCode 语言编码
     * @return 联赛球队列表
     */
    @Select("""
            <script>
            SELECT DISTINCT
                t.id AS teamId,
                t.code AS teamCode,
                ti.name AS teamName,
                ti.short_name AS shortName,
                t.logo_url AS logoUrl
            FROM (
                SELECT home_team_id AS team_id
                FROM soccer_match
                WHERE league_id = #{leagueId}

                UNION

                SELECT away_team_id AS team_id
                FROM soccer_match
                WHERE league_id = #{leagueId}
            ) mt
            INNER JOIN soccer_team t
                ON t.id = mt.team_id
            LEFT JOIN soccer_team_i18n ti
                ON ti.team_id = t.id
               AND ti.lang_code = #{langCode}
            WHERE t.status = 1
            ORDER BY
                ti.name ASC,
                t.id ASC
            </script>
            """)
    List<ChampionLeagueTeamResponse> selectLeagueTeams(
            @Param("leagueId") Long leagueId,
            @Param("langCode") String langCode
    );

    /**
     * 查询指定球队的展示名称。
     *
     * @param teamId   球队 ID
     * @param langCode 语言编码
     * @return 球队展示名称
     */
    @Select("""
            SELECT
                COALESCE(ti.name, t.code)
            FROM soccer_team t
            LEFT JOIN soccer_team_i18n ti
                ON ti.team_id = t.id
               AND ti.lang_code = #{langCode}
            WHERE t.id = #{teamId}
            LIMIT 1
            """)
    String selectTeamName(
            @Param("teamId") Long teamId,
            @Param("langCode") String langCode
    );

    /**
     * 统计指定联赛是否存在。
     *
     * @param leagueId 联赛或杯赛 ID
     * @return 匹配数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM soccer_league
            WHERE id = #{leagueId}
            """)
    Long countLeague(
            @Param("leagueId") Long leagueId
    );

    /**
     * 统计指定球队是否存在。
     *
     * @param teamId 球队 ID
     * @return 匹配数量
     */
    @Select("""
            SELECT COUNT(1)
            FROM soccer_team
            WHERE id = #{teamId}
            """)
    Long countTeam(
            @Param("teamId") Long teamId
    );
}
