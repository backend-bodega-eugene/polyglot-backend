package com.eugene.goalhub.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 冠军玩法赔率实体。
 *
 * <p>映射 champion_market_odds 表，用于维护联赛球队维度的冠军赔率。</p>
 */
@TableName("champion_market_odds")
public class ChampionMarketOddsEntity {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 联赛或杯赛 ID。
     */
    private Long leagueId;

    /**
     * 球队 ID。
     */
    private Long teamId;

    /**
     * 球队名称快照。
     */
    private String teamNameSnapshot;

    /**
     * 冠军赔率。
     */
    private BigDecimal odds;

    /**
     * 是否可见，1 可见，0 隐藏。
     */
    private Integer visible;

    /**
     * 下注状态。
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

    /**
     * 获取主键 ID。
     *
     * @return 主键 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键 ID。
     *
     * @param id 主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取联赛或杯赛 ID。
     *
     * @return 联赛或杯赛 ID
     */
    public Long getLeagueId() {
        return leagueId;
    }

    /**
     * 设置联赛或杯赛 ID。
     *
     * @param leagueId 联赛或杯赛 ID
     */
    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    /**
     * 获取球队 ID。
     *
     * @return 球队 ID
     */
    public Long getTeamId() {
        return teamId;
    }

    /**
     * 设置球队 ID。
     *
     * @param teamId 球队 ID
     */
    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    /**
     * 获取球队名称快照。
     *
     * @return 球队名称快照
     */
    public String getTeamNameSnapshot() {
        return teamNameSnapshot;
    }

    /**
     * 设置球队名称快照。
     *
     * @param teamNameSnapshot 球队名称快照
     */
    public void setTeamNameSnapshot(String teamNameSnapshot) {
        this.teamNameSnapshot = teamNameSnapshot;
    }

    /**
     * 获取冠军赔率。
     *
     * @return 冠军赔率
     */
    public BigDecimal getOdds() {
        return odds;
    }

    /**
     * 设置冠军赔率。
     *
     * @param odds 冠军赔率
     */
    public void setOdds(BigDecimal odds) {
        this.odds = odds;
    }

    /**
     * 获取是否可见。
     *
     * @return 是否可见
     */
    public Integer getVisible() {
        return visible;
    }

    /**
     * 设置是否可见。
     *
     * @param visible 是否可见，1 可见，0 隐藏
     */
    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    /**
     * 获取下注状态。
     *
     * @return 下注状态
     */
    public String getBetStatus() {
        return betStatus;
    }

    /**
     * 设置下注状态。
     *
     * @param betStatus 下注状态
     */
    public void setBetStatus(String betStatus) {
        this.betStatus = betStatus;
    }

    /**
     * 获取排序值。
     *
     * @return 排序值
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置排序值。
     *
     * @param sortOrder 排序值
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
