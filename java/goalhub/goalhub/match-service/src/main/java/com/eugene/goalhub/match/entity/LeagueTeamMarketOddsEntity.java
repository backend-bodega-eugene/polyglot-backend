package com.eugene.goalhub.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 联盟球队玩法赔率实体。
 *
 * <p>映射 league_team_market_odds 表，用于维护联赛、玩法、球队维度的赔率。</p>
 */
@TableName("league_team_market_odds")
public class LeagueTeamMarketOddsEntity {

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
     * 玩法 ID。
     */
    private Long playId;

    /**
     * 玩法编码。
     */
    private String playCode;

    /**
     * 玩法名称。
     */
    private String playName;

    /**
     * 球队 ID。
     */
    private Long teamId;

    /**
     * 球队名称快照。
     */
    private String teamNameSnapshot;

    /**
     * 玩法赔率。
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLeagueId() { return leagueId; }
    public void setLeagueId(Long leagueId) { this.leagueId = leagueId; }

    public Long getPlayId() { return playId; }
    public void setPlayId(Long playId) { this.playId = playId; }

    public String getPlayCode() { return playCode; }
    public void setPlayCode(String playCode) { this.playCode = playCode; }

    public String getPlayName() { return playName; }
    public void setPlayName(String playName) { this.playName = playName; }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }

    public String getTeamNameSnapshot() { return teamNameSnapshot; }
    public void setTeamNameSnapshot(String teamNameSnapshot) { this.teamNameSnapshot = teamNameSnapshot; }

    public BigDecimal getOdds() { return odds; }
    public void setOdds(BigDecimal odds) { this.odds = odds; }

    public Integer getVisible() { return visible; }
    public void setVisible(Integer visible) { this.visible = visible; }

    public String getBetStatus() { return betStatus; }
    public void setBetStatus(String betStatus) { this.betStatus = betStatus; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
