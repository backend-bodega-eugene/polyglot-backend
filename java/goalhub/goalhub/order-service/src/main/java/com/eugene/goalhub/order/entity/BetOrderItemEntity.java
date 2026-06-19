package com.eugene.goalhub.order.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投注订单明细实体。
 *
 * <p>映射 bet_order_item 表，保存单个投注选项的赛事、玩法、赔率、投注金额和赛果快照信息。</p>
 */
@TableName("bet_order_item")
public class BetOrderItemEntity {

    /**
     * 明细主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单 ID。
     */
    private Long orderId;

    /**
     * 订单号。
     */
    private String orderNo;

    /**
     * 比赛 ID。
     */
    private Long matchId;

    /**
     * 玩法 ID。
     */
    private Long playId;

    /**
     * 选项 ID。
     */
    private Long optionId;

    /**
     * 玩法编码。
     */
    private String playCode;

    /**
     * 玩法名称。
     */
    private String playName;

    /**
     * 选项编码。
     */
    private String optionCode;

    /**
     * 选项名称。
     */
    private String optionName;

    /**
     * 投注赔率。
     */
    private BigDecimal odds;

    /**
     * 投注金额。
     */
    private BigDecimal betAmount;

    /**
     * 预计盈利。
     */
    private BigDecimal expectedProfit;

    /**
     * 预计返还金额。
     */
    private BigDecimal expectedReturn;

    /**
     * 系统判定结果。
     */
    private String systemResult;

    /**
     * 比赛结果快照。
     */
    private String matchResultSnapshot;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 投注类型。
     */
    private String betType;

    /**
     * 冠军投注所属联赛 ID。
     */
    private Long leagueId;

    /**
     * 冠军投注所属联赛名称。
     */
    private String leagueName;

    /**
     * 冠军投注球队 ID。
     */
    private Long championTeamId;

    /**
     * 冠军投注球队名称。
     */
    private String championTeamName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getPlayId() {
        return playId;
    }

    public void setPlayId(Long playId) {
        this.playId = playId;
    }

    public Long getOptionId() {
        return optionId;
    }

    public void setOptionId(Long optionId) {
        this.optionId = optionId;
    }

    public String getPlayCode() {
        return playCode;
    }

    public void setPlayCode(String playCode) {
        this.playCode = playCode;
    }

    public String getPlayName() {
        return playName;
    }

    public void setPlayName(String playName) {
        this.playName = playName;
    }

    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public BigDecimal getOdds() {
        return odds;
    }

    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    public BigDecimal getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(BigDecimal betAmount) {
        this.betAmount = betAmount;
    }

    public BigDecimal getExpectedProfit() {
        return expectedProfit;
    }

    public void setExpectedProfit(BigDecimal expectedProfit) {
        this.expectedProfit = expectedProfit;
    }

    public BigDecimal getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(BigDecimal expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public String getSystemResult() {
        return systemResult;
    }

    public void setSystemResult(String systemResult) {
        this.systemResult = systemResult;
    }

    public String getMatchResultSnapshot() {
        return matchResultSnapshot;
    }

    public void setMatchResultSnapshot(String matchResultSnapshot) {
        this.matchResultSnapshot = matchResultSnapshot;
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

    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public String getLeagueName() {
        return leagueName;
    }

    public void setLeagueName(String leagueName) {
        this.leagueName = leagueName;
    }

    public Long getChampionTeamId() {
        return championTeamId;
    }

    public void setChampionTeamId(Long championTeamId) {
        this.championTeamId = championTeamId;
    }

    public String getChampionTeamName() {
        return championTeamName;
    }

    public void setChampionTeamName(String championTeamName) {
        this.championTeamName = championTeamName;
    }
}
