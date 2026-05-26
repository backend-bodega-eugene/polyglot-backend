package com.eugene.goalhub.match.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("soccer_match")
public class SoccerMatchEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long leagueId;

    private String matchCode;

    private String stageCode;

    private Long homeTeamId;

    private Long awayTeamId;

    private LocalDateTime scheduledStartTimeUtc;

    private LocalDateTime actualStartTimeUtc;

    private LocalDateTime actualEndTimeUtc;

    private String hostCountry;

    private String status;

    private LocalDateTime createdAt;

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