package com.eugene.goalhub.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.order.entity.BetOrderEntity;
import dto.AdminBetOrderPageRequest;
import dto.AdminBetOrderResponse;
import dto.AppBetOrderPageRequest;
import dto.AppBetOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 投注订单 Mapper。
 *
 * <p>负责投注订单表的基础 CRUD，以及后台订单分页查询。</p>
 */
@Mapper
public interface BetOrderMapper extends BaseMapper<BetOrderEntity> {

    /**
     * 分页查询后台投注订单。
     *
     * @param page    分页参数
     * @param request 投注订单查询条件
     * @return 后台投注订单分页结果
     */
    @Select("""
            <script>
            SELECT
                id AS orderId,
                order_no AS orderNo,
                user_id AS userId,
                account_id AS accountId,
                total_bet_amount AS totalBetAmount,
                total_expected_profit AS totalExpectedProfit,
                total_expected_return AS totalExpectedReturn,
                currency_code AS currencyCode,
                balance_before AS balanceBefore,
                balance_after AS balanceAfter,
                status,
                system_result AS systemResult,
                review_result AS reviewResult,
                review_admin_id AS reviewAdminId,
                review_admin_name AS reviewAdminName,
                review_remark AS reviewRemark,
                reviewed_at AS reviewedAt,
                settle_amount AS settleAmount,
                settle_admin_id AS settleAdminId,
                settle_admin_name AS settleAdminName,
                settle_remark AS settleRemark,
                settled_at AS settledAt,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM bet_order
            WHERE 1 = 1
            <if test="req.orderNo != null and req.orderNo != ''">
                AND order_no = #{req.orderNo}
            </if>
            <if test="req.userId != null">
                AND user_id = #{req.userId}
            </if>
            <if test="req.currencyCode != null and req.currencyCode != ''">
                AND currency_code = #{req.currencyCode}
            </if>
            <if test="req.status != null and req.status != ''">
                AND status = #{req.status}
            </if>
            <if test="req.systemResult != null and req.systemResult != ''">
                AND system_result = #{req.systemResult}
            </if>
            <if test="req.reviewResult != null and req.reviewResult != ''">
                AND review_result = #{req.reviewResult}
            </if>
            <if test="req.createdStartTime != null">
                AND created_at &gt;= #{req.createdStartTime}
            </if>
            <if test="req.createdEndTime != null">
                AND created_at &lt;= #{req.createdEndTime}
            </if>
            ORDER BY created_at DESC, id DESC
            </script>
            """)
    Page<AdminBetOrderResponse> selectAdminOrderPage(
            Page<AdminBetOrderResponse> page,
            @Param("req") AdminBetOrderPageRequest request
    );

    /**
     * 根据订单 ID 查询订单并加行锁。
     *
     * @param orderId 订单 ID
     * @return 投注订单
     */
    @Select("""
            SELECT *
            FROM bet_order
            WHERE id = #{orderId}
            FOR UPDATE
            """)
    BetOrderEntity selectByIdForUpdate(
            @Param("orderId") Long orderId
    );
    @Select("""
        <script>
        SELECT
            id AS orderId,
            order_no AS orderNo,
            total_bet_amount AS totalBetAmount,
            total_expected_profit AS totalExpectedProfit,
            total_expected_return AS totalExpectedReturn,
            currency_code AS currencyCode,
            status,
            system_result AS systemResult,
            review_result AS reviewResult,
            settle_amount AS settleAmount,
            settle_remark AS settleRemark,
            settled_at AS settledAt,
            created_at AS createdAt
        FROM bet_order
        WHERE user_id = #{userId}
        <choose>
            <when test="settled == true">
                AND settled_at IS NOT NULL
            </when>
            <otherwise>
                AND settled_at IS NULL
            </otherwise>
        </choose>
        ORDER BY created_at DESC, id DESC
        </script>
        """)
    Page<AppBetOrderResponse> selectAppOrderPage(
            Page<AppBetOrderResponse> page,
            @Param("userId") Long userId,
            @Param("settled") Boolean settled
    );
    @Select("""
        <script>
        SELECT
            id AS orderId,
            order_no AS orderNo,
            total_bet_amount AS totalBetAmount,
            total_expected_profit AS totalExpectedProfit,
            total_expected_return AS totalExpectedReturn,
            currency_code AS currencyCode,
            status,
            system_result AS systemResult,
            review_result AS reviewResult,
            settle_amount AS settleAmount,
            settle_remark AS settleRemark,
            settled_at AS settledAt,
            created_at AS createdAt
        FROM bet_order
        WHERE user_id = #{userId}
        <if test="req.keywords != null and req.keywords != ''">
            AND (
                order_no LIKE CONCAT('%', #{req.keywords}, '%')
                OR EXISTS (
                    SELECT 1
                    FROM bet_order_item i
                    WHERE i.order_id = bet_order.id
                      AND (
                          i.play_name LIKE CONCAT('%', #{req.keywords}, '%')
                          OR i.option_name LIKE CONCAT('%', #{req.keywords}, '%')
                          OR i.play_code LIKE CONCAT('%', #{req.keywords}, '%')
                          OR i.option_code LIKE CONCAT('%', #{req.keywords}, '%')
                      )
                )
            )
        </if>
        <if test="req.startTime != null">
            AND created_at &gt;= #{req.startTime}
        </if>
        <if test="req.endTime != null">
            AND created_at &lt;= #{req.endTime}
        </if>
        ORDER BY created_at DESC, id DESC
        </script>
        """)
    Page<AppBetOrderResponse> selectAppMyOrderPage(
            Page<AppBetOrderResponse> page,
            @Param("userId") Long userId,
            @Param("req") AppBetOrderPageRequest request
    );
}
