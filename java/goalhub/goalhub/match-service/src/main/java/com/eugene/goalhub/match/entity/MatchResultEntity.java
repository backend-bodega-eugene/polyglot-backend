package com.eugene.goalhub.match.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 比赛结果实体。
 *
 * <p>对应 match_results 表，保存比赛比分、技术统计、审核状态和审计时间。</p>
 */
@TableName("match_results")
public class MatchResultEntity {

    /**
     * 主键 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 比赛 ID。
     */
    private Long matchId;

    /**
     * 常规时间主队得分。
     */
    private Integer regularHomeScore;

    /**
     * 常规时间客队得分。
     */
    private Integer regularAwayScore;

    /**
     * 加时赛主队得分。
     */
    private Integer extraHomeScore;

    /**
     * 加时赛客队得分。
     */
    private Integer extraAwayScore;

    /**
     * 点球大战主队得分。
     */
    private Integer penaltyHomeScore;

    /**
     * 点球大战客队得分。
     */
    private Integer penaltyAwayScore;

    /**
     * 主队点球次数。
     */
    private Integer homePenaltyCount;

    /**
     * 客队点球次数。
     */
    private Integer awayPenaltyCount;

    /**
     * 主队角球次数。
     */
    private Integer homeCornerCount;

    /**
     * 客队角球次数。
     */
    private Integer awayCornerCount;

    /**
     * 主队界外球次数。
     */
    private Integer homeThrowInCount;

    /**
     * 客队界外球次数。
     */
    private Integer awayThrowInCount;

    /**
     * 主队犯规次数。
     */
    private Integer homeFoulCount;

    /**
     * 客队犯规次数。
     */
    private Integer awayFoulCount;

    /**
     * 主队任意球次数。
     */
    private Integer homeFreeKickCount;

    /**
     * 客队任意球次数。
     */
    private Integer awayFreeKickCount;

    /**
     * 主队红牌数量。
     */
    private Integer homeRedCardCount;

    /**
     * 客队红牌数量。
     */
    private Integer awayRedCardCount;

    /**
     * 主队黄牌数量。
     */
    private Integer homeYellowCardCount;

    /**
     * 客队黄牌数量。
     */
    private Integer awayYellowCardCount;

    /**
     * 比赛结束时间。
     */
    private LocalDateTime matchEndedAt;

    /**
     * 结果状态。
     */
    private Integer status;

    /**
     * 创建人 ID。
     */
    private Long createdBy;

    /**
     * 更新人 ID。
     */
    private Long updatedBy;

    /**
     * 审核人 ID。
     */
    private Long approvedBy;

    /**
     * 审核时间。
     */
    private LocalDateTime approvedAt;

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

    public Integer getRegularHomeScore() {
        return regularHomeScore;
    }

    public void setRegularHomeScore(Integer regularHomeScore) {
        this.regularHomeScore = regularHomeScore;
    }

    public Integer getRegularAwayScore() {
        return regularAwayScore;
    }

    public void setRegularAwayScore(Integer regularAwayScore) {
        this.regularAwayScore = regularAwayScore;
    }

    public Integer getExtraHomeScore() {
        return extraHomeScore;
    }

    public void setExtraHomeScore(Integer extraHomeScore) {
        this.extraHomeScore = extraHomeScore;
    }

    public Integer getExtraAwayScore() {
        return extraAwayScore;
    }

    public void setExtraAwayScore(Integer extraAwayScore) {
        this.extraAwayScore = extraAwayScore;
    }

    public Integer getPenaltyHomeScore() {
        return penaltyHomeScore;
    }

    public void setPenaltyHomeScore(Integer penaltyHomeScore) {
        this.penaltyHomeScore = penaltyHomeScore;
    }

    public Integer getPenaltyAwayScore() {
        return penaltyAwayScore;
    }

    public void setPenaltyAwayScore(Integer penaltyAwayScore) {
        this.penaltyAwayScore = penaltyAwayScore;
    }

    public Integer getHomePenaltyCount() {
        return homePenaltyCount;
    }

    public void setHomePenaltyCount(Integer homePenaltyCount) {
        this.homePenaltyCount = homePenaltyCount;
    }

    public Integer getAwayPenaltyCount() {
        return awayPenaltyCount;
    }

    public void setAwayPenaltyCount(Integer awayPenaltyCount) {
        this.awayPenaltyCount = awayPenaltyCount;
    }

    public Integer getHomeCornerCount() {
        return homeCornerCount;
    }

    public void setHomeCornerCount(Integer homeCornerCount) {
        this.homeCornerCount = homeCornerCount;
    }

    public Integer getAwayCornerCount() {
        return awayCornerCount;
    }

    public void setAwayCornerCount(Integer awayCornerCount) {
        this.awayCornerCount = awayCornerCount;
    }

    public Integer getHomeThrowInCount() {
        return homeThrowInCount;
    }

    public void setHomeThrowInCount(Integer homeThrowInCount) {
        this.homeThrowInCount = homeThrowInCount;
    }

    public Integer getAwayThrowInCount() {
        return awayThrowInCount;
    }

    public void setAwayThrowInCount(Integer awayThrowInCount) {
        this.awayThrowInCount = awayThrowInCount;
    }

    public Integer getHomeFoulCount() {
        return homeFoulCount;
    }

    public void setHomeFoulCount(Integer homeFoulCount) {
        this.homeFoulCount = homeFoulCount;
    }

    public Integer getAwayFoulCount() {
        return awayFoulCount;
    }

    public void setAwayFoulCount(Integer awayFoulCount) {
        this.awayFoulCount = awayFoulCount;
    }

    public Integer getHomeFreeKickCount() {
        return homeFreeKickCount;
    }

    public void setHomeFreeKickCount(Integer homeFreeKickCount) {
        this.homeFreeKickCount = homeFreeKickCount;
    }

    public Integer getAwayFreeKickCount() {
        return awayFreeKickCount;
    }

    public void setAwayFreeKickCount(Integer awayFreeKickCount) {
        this.awayFreeKickCount = awayFreeKickCount;
    }

    public Integer getHomeRedCardCount() {
        return homeRedCardCount;
    }

    public void setHomeRedCardCount(Integer homeRedCardCount) {
        this.homeRedCardCount = homeRedCardCount;
    }

    public Integer getAwayRedCardCount() {
        return awayRedCardCount;
    }

    public void setAwayRedCardCount(Integer awayRedCardCount) {
        this.awayRedCardCount = awayRedCardCount;
    }

    public Integer getHomeYellowCardCount() {
        return homeYellowCardCount;
    }

    public void setHomeYellowCardCount(Integer homeYellowCardCount) {
        this.homeYellowCardCount = homeYellowCardCount;
    }

    public Integer getAwayYellowCardCount() {
        return awayYellowCardCount;
    }

    public void setAwayYellowCardCount(Integer awayYellowCardCount) {
        this.awayYellowCardCount = awayYellowCardCount;
    }

    public LocalDateTime getMatchEndedAt() {
        return matchEndedAt;
    }

    public void setMatchEndedAt(LocalDateTime matchEndedAt) {
        this.matchEndedAt = matchEndedAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
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
