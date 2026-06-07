package com.eugene.goalhub.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户充值订单实体。
 *
 * <p>映射用户充值订单表，记录充值金额、链路信息、审核信息和时间信息。</p>
 */
@TableName("user_deposit_order")
public class DepositOrderEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 充值订单号。
     */
    private String orderNo;

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 币种编码。
     */
    private String currencyCode;

    /**
     * 用户提交的充值金额。
     */
    private BigDecimal amount;

    /**
     * 实际入账金额。
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态。
     */
    private String status;

    /**
     * 区块链网络类型。
     */
    private String chainType;

    /**
     * 链上交易哈希。
     */
    private String txHash;

    /**
     * 用户备注。
     */
    private String remark;

    /**
     * 审核备注。
     */
    private String auditRemark;

    /**
     * 审核管理员 ID。
     */
    private Long auditAdminId;

    /**
     * 审核管理员名称。
     */
    private String auditAdminName;

    /**
     * 审核时间。
     */
    private LocalDateTime auditTime;

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

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChainType() {
        return chainType;
    }

    public void setChainType(String chainType) {
        this.chainType = chainType;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public Long getAuditAdminId() {
        return auditAdminId;
    }

    public void setAuditAdminId(Long auditAdminId) {
        this.auditAdminId = auditAdminId;
    }

    public String getAuditAdminName() {
        return auditAdminName;
    }

    public void setAuditAdminName(String auditAdminName) {
        this.auditAdminName = auditAdminName;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
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
