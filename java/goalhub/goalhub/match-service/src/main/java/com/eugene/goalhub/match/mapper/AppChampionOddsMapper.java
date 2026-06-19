package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import dto.AppChampionOddsPageRequest;
import dto.AppChampionOddsResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * App 冠军赔率查询 Mapper。
 */
@Mapper
public interface AppChampionOddsMapper {

    /**
     * 分页查询前端可见且可下注的冠军赔率。
     *
     * @param page    MyBatis-Plus 分页参数
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页数据
     */
    @Select("""
            <script>
            SELECT
                co.id AS championOddsId,

                co.league_id AS leagueId,
                li.name AS leagueName,
                l.logo_url AS leagueLogoUrl,

                co.team_id AS teamId,
                t.code AS teamCode,
                COALESCE(ti.name, co.team_name_snapshot) AS teamName,
                t.logo_url AS teamLogoUrl,

                co.odds,
                co.visible,
                co.bet_status AS betStatus,
                co.sort_order AS sortOrder

            FROM champion_market_odds co

            INNER JOIN soccer_league l
                ON l.id = co.league_id
               AND l.status = 1

            INNER JOIN soccer_team t
                ON t.id = co.team_id
               AND t.status = 1

            LEFT JOIN soccer_league_i18n li
                ON li.league_id = co.league_id
               AND li.lang_code = #{req.langCode}

            LEFT JOIN soccer_team_i18n ti
                ON ti.team_id = co.team_id
               AND ti.lang_code = #{req.langCode}

            WHERE co.visible = 1
              AND co.bet_status = 'OPEN'

            <if test="req.leagueId != null">
                AND co.league_id = #{req.leagueId}
            </if>

            ORDER BY
                co.league_id ASC,
                co.sort_order ASC,
                co.id ASC
            </script>
            """)
    Page<AppChampionOddsResponse> pageChampionOdds(
            Page<AppChampionOddsResponse> page,
            @Param("req") AppChampionOddsPageRequest request
    );
}
