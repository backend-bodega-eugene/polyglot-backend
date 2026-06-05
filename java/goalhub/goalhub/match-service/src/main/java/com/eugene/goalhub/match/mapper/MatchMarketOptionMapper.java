package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.MatchMarketOptionEntity;
import dto.MatchMarketOptionPageRequest;
import dto.MatchMarketOptionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 赛事玩法赔率 Mapper。
 *
 * <p>负责赛事玩法赔率表的通用 CRUD 和后台分页查询。</p>
 */
@Mapper
public interface MatchMarketOptionMapper extends BaseMapper<MatchMarketOptionEntity> {

    /**
     * 分页查询后台赛事玩法赔率列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台赛事玩法赔率分页数据
     */
    @Select("""
            <script>
            SELECT
                mo.id,
                mo.match_id AS matchId,
                m.match_code AS matchCode,
                mi.match_name AS matchName,

                mo.market_id AS marketId,
                mo.market_option_id AS marketOptionId,

                mo.market_code AS marketCode,
                mo.market_name AS marketName,
                mo.market_option_code AS marketOptionCode,
                mo.market_option_name AS marketOptionName,

                mo.odds,
                mo.visible,
                mo.bet_status AS betStatus,
                mo.sort_order AS sortOrder,
                mo.created_at AS createdAt,
                mo.updated_at AS updatedAt

            FROM match_market_option mo

            LEFT JOIN soccer_match m
                ON m.id = mo.match_id

            LEFT JOIN soccer_match_i18n mi
                ON mi.match_id = mo.match_id
               AND mi.lang_code = #{req.langCode}

            WHERE 1 = 1

            <if test="req.matchId != null">
                AND mo.match_id = #{req.matchId}
            </if>

            <if test="req.marketId != null">
                AND mo.market_id = #{req.marketId}
            </if>

            <if test="req.marketOptionId != null">
                AND mo.market_option_id = #{req.marketOptionId}
            </if>

            <if test="req.visible != null">
                AND mo.visible = #{req.visible}
            </if>

            <if test="req.betStatus != null and req.betStatus != ''">
                AND mo.bet_status = #{req.betStatus}
            </if>

            <if test="req.keyword != null and req.keyword != ''">
                AND (
                    mi.match_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mo.market_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mo.market_option_name LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mo.market_code LIKE CONCAT('%', #{req.keyword}, '%')
                    OR mo.market_option_code LIKE CONCAT('%', #{req.keyword}, '%')
                )
            </if>

            ORDER BY
                mo.match_id DESC,
                mo.market_id ASC,
                mo.sort_order ASC,
                mo.id DESC

            </script>
            """)
    Page<MatchMarketOptionResponse> adminPage(
            Page<MatchMarketOptionResponse> page,
            @Param("req") MatchMarketOptionPageRequest request
    );
}
