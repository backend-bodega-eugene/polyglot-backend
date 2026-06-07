package com.eugene.goalhub.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.order.entity.BetOrderItemEntity;
import dto.AdminBetOrderItemPageRequest;
import dto.AdminBetOrderItemResponse;
import dto.AppBetOrderItemResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 投注订单明细 Mapper。
 *
 * <p>负责投注订单明细表的基础 CRUD，以及后台订单明细分页查询。</p>
 */
@Mapper
public interface BetOrderItemMapper
        extends BaseMapper<BetOrderItemEntity> {

    /**
     * 分页查询后台投注订单明细。
     *
     * @param page    分页参数
     * @param request 投注订单明细查询条件
     * @return 后台投注订单明细分页结果
     */
    @Select("""
            <script>
            SELECT
                i.id AS itemId,
                i.order_id AS orderId,
                i.order_no AS orderNo,
                i.match_id AS matchId,

                m.league_id AS leagueId,
                li.name AS leagueName,

                m.home_team_id AS homeTeamId,
                hti.name AS homeTeamName,

                m.away_team_id AS awayTeamId,
                ati.name AS awayTeamName,

                m.scheduled_start_time_utc AS matchStartTime,

                i.play_id AS playId,
                i.option_id AS optionId,

                i.play_code AS playCode,
                i.play_name AS playName,
                i.option_code AS optionCode,
                i.option_name AS optionName,

                i.odds,
                i.bet_amount AS betAmount,
                i.expected_profit AS expectedProfit,
                i.expected_return AS expectedReturn,

                i.system_result AS systemResult,
                i.match_result_snapshot AS matchResultSnapshot,
                i.created_at AS createdAt
            FROM bet_order_item i
            LEFT JOIN soccer_match m
                ON m.id = i.match_id
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
            <if test="req.orderId != null">
                AND i.order_id = #{req.orderId}
            </if>
            <if test="req.orderNo != null and req.orderNo != ''">
                AND i.order_no = #{req.orderNo}
            </if>
            ORDER BY i.id ASC
            </script>
            """)
    Page<AdminBetOrderItemResponse> selectAdminOrderItemPage(
            Page<AdminBetOrderItemResponse> page,
            @Param("req") AdminBetOrderItemPageRequest request
    );
    @Select("""
        <script>
        SELECT
            i.id AS itemId,
            i.order_id AS orderId,
            i.order_no AS orderNo,
            i.match_id AS matchId,

            m.league_id AS leagueId,
            li.name AS leagueName,

            m.home_team_id AS homeTeamId,
            hti.name AS homeTeamName,

            m.away_team_id AS awayTeamId,
            ati.name AS awayTeamName,

            m.scheduled_start_time_utc AS matchStartTime,

            i.play_id AS playId,
            i.option_id AS optionId,

            i.play_code AS playCode,
            i.play_name AS playName,
            i.option_code AS optionCode,
            i.option_name AS optionName,

            i.odds,
            i.bet_amount AS betAmount,
            i.expected_profit AS expectedProfit,
            i.expected_return AS expectedReturn,

            i.system_result AS systemResult,
            i.match_result_snapshot AS matchResultSnapshot,
            i.created_at AS createdAt
        FROM bet_order_item i
        LEFT JOIN soccer_match m
            ON m.id = i.match_id
        LEFT JOIN soccer_league_i18n li
            ON li.league_id = m.league_id
           AND li.lang_code = #{langCode}
        LEFT JOIN soccer_team_i18n hti
            ON hti.team_id = m.home_team_id
           AND hti.lang_code = #{langCode}
        LEFT JOIN soccer_team_i18n ati
            ON ati.team_id = m.away_team_id
           AND ati.lang_code = #{langCode}
        WHERE i.order_id IN
        <foreach collection="orderIds" item="orderId" open="(" separator="," close=")">
            #{orderId}
        </foreach>
        ORDER BY i.order_id DESC, i.id ASC
        </script>
        """)
    List<AppBetOrderItemResponse> selectAppOrderItemsByOrderIds(
            @Param("orderIds") List<Long> orderIds,
            @Param("langCode") String langCode
    );
}
