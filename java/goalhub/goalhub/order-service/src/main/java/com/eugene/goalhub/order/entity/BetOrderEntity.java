package com.eugene.goalhub.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投注订单实体。
 *
 * <p>映射 bet_order 表，记录投注订单主信息、金额快照、审核信息和结算信息。</p>
 */
@TableName("bet_order")
public class BetOrderEntity {

    /**
     * 订单主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号。
     */
    private String orderNo;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 用户账户 ID。
     */
    private Long accountId;

    /**
     * 总投注金额。
     */
    private BigDecimal totalBetAmount;

    /**
     * 总预计盈利。
     */
    private BigDecimal totalExpectedProfit;

    /**
     * 总预计返还金额。
     */
    private BigDecimal totalExpectedReturn;

    /**
     * 币种编码。
     */
    private String currencyCode;

    /**
     * 投注前账户余额。
     */
    private BigDecimal balanceBefore;

    /**
     * 投注后账户余额。
     */
    private BigDecimal balanceAfter;

    /**
     * 订单状态。
     */
    private String status;

    /**
     * 系统判定结果。
     */
    private String systemResult;

    /**
     * 人工审核结果。
     */
    private String reviewResult;

    /**
     * 审核管理员 ID。
     */
    private Long reviewAdminId;

    /**
     * 审核管理员名称。
     */
    private String reviewAdminName;

    /**
     * 审核备注。
     */
    private String reviewRemark;

    /**
     * 审核时间。
     */
    private LocalDateTime reviewedAt;

    /**
     * 结算金额。
     */
    private BigDecimal settleAmount;

    /**
     * 结算管理员 ID。
     */
    private Long settleAdminId;

    /**
     * 结算管理员名称。
     */
    private String settleAdminName;

    /**
     * 结算备注。
     */
    private String settleRemark;

    /**
     * 结算时间。
     */
    private LocalDateTime settledAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getTotalBetAmount() {
        return totalBetAmount;
    }

    public void setTotalBetAmount(BigDecimal totalBetAmount) {
        this.totalBetAmount = totalBetAmount;
    }

    public BigDecimal getTotalExpectedProfit() {
        return totalExpectedProfit;
    }

    public void setTotalExpectedProfit(BigDecimal totalExpectedProfit) {
        this.totalExpectedProfit = totalExpectedProfit;
    }

    public BigDecimal getTotalExpectedReturn() {
        return totalExpectedReturn;
    }

    public void setTotalExpectedReturn(BigDecimal totalExpectedReturn) {
        this.totalExpectedReturn = totalExpectedReturn;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getBalanceBefore() {
        return balanceBefore;
    }

    public void setBalanceBefore(BigDecimal balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSystemResult() {
        return systemResult;
    }

    public void setSystemResult(String systemResult) {
        this.systemResult = systemResult;
    }

    public String getReviewResult() {
        return reviewResult;
    }

    public void setReviewResult(String reviewResult) {
        this.reviewResult = reviewResult;
    }

    public Long getReviewAdminId() {
        return reviewAdminId;
    }

    public void setReviewAdminId(Long reviewAdminId) {
        this.reviewAdminId = reviewAdminId;
    }

    public String getReviewAdminName() {
        return reviewAdminName;
    }

    public void setReviewAdminName(String reviewAdminName) {
        this.reviewAdminName = reviewAdminName;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public BigDecimal getSettleAmount() {
        return settleAmount;
    }

    public void setSettleAmount(BigDecimal settleAmount) {
        this.settleAmount = settleAmount;
    }

    public Long getSettleAdminId() {
        return settleAdminId;
    }

    public void setSettleAdminId(Long settleAdminId) {
        this.settleAdminId = settleAdminId;
    }

    public String getSettleAdminName() {
        return settleAdminName;
    }

    public void setSettleAdminName(String settleAdminName) {
        this.settleAdminName = settleAdminName;
    }

    public String getSettleRemark() {
        return settleRemark;
    }

    public void setSettleRemark(String settleRemark) {
        this.settleRemark = settleRemark;
    }

    public LocalDateTime getSettledAt() {
        return settledAt;
    }

    public void setSettledAt(LocalDateTime settledAt) {
        this.settledAt = settledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
