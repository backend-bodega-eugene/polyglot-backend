package com.eugene.goalhub.match.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 赛事玩法赔率实体，对应 match_market_option 表。
 *
 * <p>保存指定比赛下玩法选项的赔率、展示状态、投注状态和玩法快照。</p>
 */
@TableName("match_market_option")
public class MatchMarketOptionEntity {

    /**
     * 赛事玩法赔率 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 比赛 ID。
     */
    private Long matchId;

    /**
     * 投注玩法 ID。
     */
    private Long marketId;

    /**
     * 投注玩法选项 ID。
     */
    private Long marketOptionId;

    /**
     * 投注玩法编码快照。
     */
    private String marketCode;

    /**
     * 投注玩法名称快照。
     */
    private String marketName;

    /**
     * 投注玩法选项编码快照。
     */
    private String marketOptionCode;

    /**
     * 投注玩法选项名称快照。
     */
    private String marketOptionName;

    /**
     * 赔率。
     */
    private BigDecimal odds;

    /**
     * 是否前端可见。
     */
    private Integer visible;

    /**
     * 投注状态。
     */
    private String betStatus;

    /**
     * 排序值。
     */
    private Integer sortOrder;

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

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getMarketId() {
        return marketId;
    }

    public void setMarketId(Long marketId) {
        this.marketId = marketId;
    }

    public Long getMarketOptionId() {
        return marketOptionId;
    }

    public void setMarketOptionId(Long marketOptionId) {
        this.marketOptionId = marketOptionId;
    }

    public String getMarketCode() {
        return marketCode;
    }

    public void setMarketCode(String marketCode) {
        this.marketCode = marketCode;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getMarketOptionCode() {
        return marketOptionCode;
    }

    public void setMarketOptionCode(String marketOptionCode) {
        this.marketOptionCode = marketOptionCode;
    }

    public String getMarketOptionName() {
        return marketOptionName;
    }

    public void setMarketOptionName(String marketOptionName) {
        this.marketOptionName = marketOptionName;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public String getBetStatus() {
        return betStatus;
    }

    public void setBetStatus(String betStatus) {
        this.betStatus = betStatus;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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
