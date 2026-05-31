package com.eugene.goalhub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.user.entity.AccountTransactionEntity;
import dto.AdminAccountTransactionPageRequest;
import dto.AdminAccountTransactionResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 账户流水 Mapper。
 */
@Mapper
public interface AccountTransactionMapper extends BaseMapper<AccountTransactionEntity> {

    /**
     * 分页查询后台账户流水列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台账户流水分页数据
     */
    @Select("""
            <script>
            SELECT
                t.id AS id,
                t.account_id AS accountId,
                t.user_id AS userId,
                u.username AS username,
                t.currency_code AS currencyCode,
                t.biz_type AS bizType,
                t.biz_id AS bizId,
                t.change_amount AS changeAmount,
                t.before_balance AS beforeBalance,
                t.after_balance AS afterBalance,
                t.remark AS remark,
                t.created_at AS createdAt
            FROM account_transactions t
            INNER JOIN users u ON t.user_id = u.id
            WHERE 1 = 1
            <if test="request.username != null and request.username != ''">
                AND u.username LIKE CONCAT('%', #{request.username}, '%')
            </if>
            <if test="request.currencyCode != null and request.currencyCode != ''">
                AND t.currency_code = #{request.currencyCode}
            </if>
            <if test="request.bizType != null and request.bizType != ''">
                AND t.biz_type = #{request.bizType}
            </if>
            <if test="request.minAmount != null">
                AND t.change_amount &gt;= #{request.minAmount}
            </if>
            <if test="request.maxAmount != null">
                AND t.change_amount &lt;= #{request.maxAmount}
            </if>
            <if test="request.startTime != null">
                AND t.created_at &gt;= #{request.startTime}
            </if>
            <if test="request.endTime != null">
                AND t.created_at &lt;= #{request.endTime}
            </if>
            ORDER BY t.id DESC
            </script>
            """)
    Page<AdminAccountTransactionResponse> adminTransactionPage(
            Page<AdminAccountTransactionResponse> page,
            @Param("request") AdminAccountTransactionPageRequest request
    );
}
