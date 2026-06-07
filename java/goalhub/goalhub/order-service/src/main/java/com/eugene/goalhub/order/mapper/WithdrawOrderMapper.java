package com.eugene.goalhub.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.order.entity.WithdrawOrderEntity;
import dto.AdminWithdrawOrderPageRequest;
import dto.AdminWithdrawOrderResponse;
import dto.AppWithdrawOrderPageRequest;
import dto.AppWithdrawOrderResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface WithdrawOrderMapper extends BaseMapper<WithdrawOrderEntity> {

    @Select("""
            <script>
            SELECT
                id,
                order_no AS orderNo,
                user_id AS userId,
                currency_code AS currencyCode,
                amount,
                actual_amount AS actualAmount,
                fee_amount AS feeAmount,
                status,
                chain_type AS chainType,
                withdraw_address AS withdrawAddress,
                tx_hash AS txHash,
                remark,
                audit_remark AS auditRemark,
                audit_admin_id AS auditAdminId,
                audit_admin_name AS auditAdminName,
                audit_time AS auditTime,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM user_withdraw_order
            WHERE 1 = 1
            <if test="req.userId != null">
                AND user_id = #{req.userId}
            </if>
            <if test="req.orderNo != null and req.orderNo != ''">
                AND order_no = #{req.orderNo}
            </if>
            <if test="req.currencyCode != null and req.currencyCode != ''">
                AND currency_code = #{req.currencyCode}
            </if>
            <if test="req.status != null and req.status != ''">
                AND status = #{req.status}
            </if>
            <if test="req.chainType != null and req.chainType != ''">
                AND chain_type = #{req.chainType}
            </if>
            <if test="req.withdrawAddress != null and req.withdrawAddress != ''">
                AND withdraw_address = #{req.withdrawAddress}
            </if>
            <if test="req.txHash != null and req.txHash != ''">
                AND tx_hash = #{req.txHash}
            </if>
            ORDER BY created_at DESC, id DESC
            </script>
            """)
    Page<AdminWithdrawOrderResponse> selectAdminPage(
            Page<AdminWithdrawOrderResponse> page,
            @Param("req") AdminWithdrawOrderPageRequest request
    );

    @Select("""
            SELECT *
            FROM user_withdraw_order
            WHERE id = #{id}
            FOR UPDATE
            """)
    WithdrawOrderEntity selectByIdForUpdate(@Param("id") Long id);
    @Select("""
        <script>
        SELECT
            id,
            order_no AS orderNo,
            currency_code AS currencyCode,
            amount,
            actual_amount AS actualAmount,
            fee_amount AS feeAmount,
            status,
            chain_type AS chainType,
            withdraw_address AS withdrawAddress,
            tx_hash AS txHash,
            remark,
            audit_remark AS auditRemark,
            audit_time AS auditTime,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM user_withdraw_order
        WHERE user_id = #{userId}
        <if test="req.orderNo != null and req.orderNo != ''">
            AND order_no = #{req.orderNo}
        </if>
        <if test="req.currencyCode != null and req.currencyCode != ''">
            AND currency_code = #{req.currencyCode}
        </if>
        <if test="req.status != null and req.status != ''">
            AND status = #{req.status}
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
    Page<AppWithdrawOrderResponse> selectAppPage(
            Page<AppWithdrawOrderResponse> page,
            @Param("userId") Long userId,
            @Param("req") AppWithdrawOrderPageRequest request
    );
}