package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dto.AppMatchMarketFlatResponse;
import dto.AppMatchMarketQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * App 赛事玩法赔率聚合查询 Mapper。
 */
@Mapper
public interface AppMatchMarketMapper {

    /**
     * 分页查询赛事玩法赔率扁平数据。
     *
     * <p>按查询类型筛选今日、滚球、早盘或串关赛事，再由服务层聚合为联赛、比赛、玩法树。</p>
     *
     * @param page      MyBatis-Plus 分页参数
     * @param request   App 赛事玩法赔率查询参数
     * @param type      查询类型
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @return 赛事玩法赔率扁平分页数据
     */
    @Select("""
            <script>
            SELECT
                l.id AS leagueId,
                li.name AS leagueName,
                l.logo_url AS leagueLogoUrl,

                m.id AS matchId,
                m.match_code AS matchCode,
                mi.match_name AS matchName,
                m.status AS matchStatus,
                DATE_FORMAT(m.scheduled_start_time_utc, '%Y-%m-%d %H:%i:%s') AS scheduledStartTimeUtc,

                m.home_team_id AS homeTeamId,
                hti.name AS homeTeamName,
                ht.logo_url AS homeTeamLogoUrl,

                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,
                at.logo_url AS awayTeamLogoUrl,

                mo.market_id AS marketId,
                mo.market_code AS marketCode,
                mo.market_name AS marketName,

                mo.id AS optionId,
                mo.market_option_id AS marketOptionId,
                mo.market_option_code AS marketOptionCode,
                mo.market_option_name AS marketOptionName,

                mo.odds,
                mo.bet_status AS betStatus,
                mo.sort_order AS sortOrder

            FROM soccer_match m

            INNER JOIN match_market_option mo
                ON mo.match_id = m.id
               AND mo.visible = 1
               AND mo.bet_status = 'OPEN'

            INNER JOIN soccer_league l
                ON l.id = m.league_id
               AND l.status = 1

            LEFT JOIN soccer_league_i18n li
                ON li.league_id = m.league_id
               AND li.lang_code = #{req.langCode}

            LEFT JOIN soccer_match_i18n mi
                ON mi.match_id = m.id
               AND mi.lang_code = #{req.langCode}

            LEFT JOIN soccer_team ht
                ON ht.id = m.home_team_id

            LEFT JOIN soccer_team_i18n hti
                ON hti.team_id = m.home_team_id
               AND hti.lang_code = #{req.langCode}

            LEFT JOIN soccer_team at
                ON at.id = m.away_team_id

            LEFT JOIN soccer_team_i18n ati
                ON ati.team_id = m.away_team_id
               AND ati.lang_code = #{req.langCode}

            WHERE 1 = 1

            <if test="req.leagueId != null">
                AND m.league_id = #{req.leagueId}
            </if>

            <if test="req.keyword != null and req.keyword != ''">
                AND (
                    li.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mi.match_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR hti.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR ati.name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mo.market_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mo.market_option_name LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>

            <choose>
                <when test="type == 'TODAY'">
                    AND m.scheduled_start_time_utc &gt;= #{startTime}
                    AND m.scheduled_start_time_utc &lt;= #{endTime}
                    AND m.status NOT IN ('FINISHED', 'CANCELLED')
                </when>

                <when test="type == 'LIVE'">
                    AND m.status = 'LIVE'
                </when>

                <when test="type == 'EARLY'">
                    AND m.scheduled_start_time_utc &gt;= #{startTime}
                    AND m.scheduled_start_time_utc &lt;= #{endTime}
                    AND m.status = 'NOT_STARTED'
                </when>

                <when test="type == 'PARLAY'">
                    AND m.status IN ('NOT_STARTED', 'LIVE')
                </when>
            </choose>

            ORDER BY
                m.scheduled_start_time_utc ASC,
                m.id ASC,
                mo.market_id ASC,
                mo.sort_order ASC,
                mo.id ASC
            </script>
            """)
    Page<AppMatchMarketFlatResponse> pageFlat(
            Page<AppMatchMarketFlatResponse> page,
            @Param("req") AppMatchMarketQueryRequest request,
            @Param("type") String type,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );
}
