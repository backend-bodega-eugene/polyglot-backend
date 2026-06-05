package com.eugene.goalhub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.user.entity.UserAccountEntity;
import dto.AdminUserAccountPageRequest;
import dto.AdminUserAccountResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户账户 Mapper。
 *
 * <p>负责用户账户表的基础 CRUD、后台账户分页查询和带行锁的账户查询。</p>
 */
@Mapper
public interface UserAccountMapper extends BaseMapper<UserAccountEntity> {

    /**
     * 分页查询后台用户账户列表。
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 后台用户账户分页数据
     */
    @Select("""
            <script>
            SELECT
                a.id AS accountId,
                a.user_id AS userId,
                u.username AS username,
                a.currency_code AS currencyCode,
                a.balance AS balance,
                a.frozen_balance AS frozenBalance,
                (a.balance - a.frozen_balance) AS availableBalance,
                a.status AS status,
                u.created_at AS userCreatedAt,
                a.created_at AS accountCreatedAt
            FROM user_accounts a
            INNER JOIN users u ON a.user_id = u.id
            WHERE 1 = 1
            <if test="request.username != null and request.username != ''">
                AND u.username LIKE CONCAT('%', #{request.username}, '%')
            </if>
            <if test="request.currencyCode != null and request.currencyCode != ''">
                AND a.currency_code = #{request.currencyCode}
            </if>
            <if test="request.minBalance != null">
                AND a.balance &gt;= #{request.minBalance}
            </if>
            <if test="request.maxBalance != null">
                AND a.balance &lt;= #{request.maxBalance}
            </if>
            <if test="request.status != null">
                AND a.status = #{request.status}
            </if>
            <if test="request.userCreatedStartTime != null">
                AND u.created_at &gt;= #{request.userCreatedStartTime}
            </if>
            <if test="request.userCreatedEndTime != null">
                AND u.created_at &lt;= #{request.userCreatedEndTime}
            </if>
            ORDER BY a.id DESC
            </script>
            """)
    Page<AdminUserAccountResponse> adminAccountPage(
            Page<AdminUserAccountResponse> page,
            @Param("request") AdminUserAccountPageRequest request
    );

    /**
     * 根据账户 ID 查询账户并加行锁。
     *
     * @param accountId 账户 ID
     * @return 用户账户实体
     */
    @Select("""
        SELECT *
        FROM user_accounts
        WHERE id = #{accountId}
        FOR UPDATE
        """)
    UserAccountEntity selectByIdForUpdate(
            @Param("accountId") Long accountId);
    /**
     * 根据用户 ID 和币种查询账户并加行锁。
     *
     * @param userId       用户 ID
     * @param currencyCode 币种编码
     * @return 用户账户实体
     */
    @Select("""
        SELECT *
        FROM user_accounts
        WHERE user_id = #{userId}
          AND currency_code = #{currencyCode}
        FOR UPDATE
        """)
    UserAccountEntity selectByUserIdAndCurrencyForUpdate(
            @Param("userId") Long userId,
            @Param("currencyCode") String currencyCode);
}
