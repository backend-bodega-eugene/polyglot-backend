package com.eugene.goalhub.match.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 足球比赛实体，对应 soccer_match 表。
 */
@TableName("soccer_match")
public class SoccerMatchEntity {

    /**
     * 赛事 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属联赛 ID。
     */
    private Long leagueId;

    /**
     * 赛事编码。
     */
    private String matchCode;

    /**
     * 阶段编码。
     */
    private String stageCode;

    /**
     * 主队 ID。
     */
    private Long homeTeamId;

    /**
     * 客队 ID。
     */
    private Long awayTeamId;

    /**
     * 计划开赛时间，UTC 时间。
     */
    private LocalDateTime scheduledStartTimeUtc;

    /**
     * 实际开赛时间，UTC 时间。
     */
    private LocalDateTime actualStartTimeUtc;

    /**
     * 实际结束时间，UTC 时间。
     */
    private LocalDateTime actualEndTimeUtc;

    /**
     * 主办国家。
     */
    private String hostCountry;

    /**
     * 赛事状态。
     */
    private String status;

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

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
    }

    public String getMatchCode() {
        return matchCode;
    }

    public void setMatchCode(String matchCode) {
        this.matchCode = matchCode;
    }

    public String getStageCode() {
        return stageCode;
    }

    public void setStageCode(String stageCode) {
        this.stageCode = stageCode;
    }

    public Long getHomeTeamId() {
        return homeTeamId;
    }

    public void setHomeTeamId(Long homeTeamId) {
        this.homeTeamId = homeTeamId;
    }

    public Long getAwayTeamId() {
        return awayTeamId;
    }

    public void setAwayTeamId(Long awayTeamId) {
        this.awayTeamId = awayTeamId;
    }

    public LocalDateTime getScheduledStartTimeUtc() {
        return scheduledStartTimeUtc;
    }

    public void setScheduledStartTimeUtc(LocalDateTime scheduledStartTimeUtc) {
        this.scheduledStartTimeUtc = scheduledStartTimeUtc;
    }

    public LocalDateTime getActualStartTimeUtc() {
        return actualStartTimeUtc;
    }

    public void setActualStartTimeUtc(LocalDateTime actualStartTimeUtc) {
        this.actualStartTimeUtc = actualStartTimeUtc;
    }

    public LocalDateTime getActualEndTimeUtc() {
        return actualEndTimeUtc;
    }

    public void setActualEndTimeUtc(LocalDateTime actualEndTimeUtc) {
        this.actualEndTimeUtc = actualEndTimeUtc;
    }

    public String getHostCountry() {
        return hostCountry;
    }

    public void setHostCountry(String hostCountry) {
        this.hostCountry = hostCountry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
