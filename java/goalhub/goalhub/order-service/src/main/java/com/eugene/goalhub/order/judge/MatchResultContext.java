package com.eugene.goalhub.order.judge;



/**
 * 赛事结果上下文。
 *
 * <p>封装常规时间、加时赛、点球和总比分，供投注结果预判逻辑使用。</p>
 */

public class MatchResultContext {

    /**
     * 比赛 ID。
     */
    private Long matchId;

    /**
     * 常规时间主队得分。
     */
    private Integer homeScore90;

    /**
     * 常规时间客队得分。
     */
    private Integer awayScore90;

    /**
     * 加时赛主队得分。
     */
    private Integer homeScoreExtra;

    /**
     * 加时赛客队得分。
     */
    private Integer awayScoreExtra;

    /**
     * 点球大战主队得分。
     */
    private Integer homeScorePenalty;

    /**
     * 点球大战客队得分。
     */
    private Integer awayScorePenalty;

    /**
     * 主队总得分。
     */
    private Integer homeScoreTotal;

    /**
     * 客队总得分。
     */
    private Integer awayScoreTotal;

    /**
     * 获取常规时间主队安全得分。
     *
     * @return 常规时间主队得分，空值按 0 处理
     */
    public int homeScore90Safe() {
        return homeScore90 == null ? 0 : homeScore90;
    }

    /**
     * 获取常规时间客队安全得分。
     *
     * @return 常规时间客队得分，空值按 0 处理
     */
    public int awayScore90Safe() {
        return awayScore90 == null ? 0 : awayScore90;
    }

    /**
     * 计算常规时间总进球数。
     *
     * @return 常规时间总进球数
     */
    public int totalGoals90() {
        return homeScore90Safe() + awayScore90Safe();
    }

    /**
     * 计算常规时间主客队分差。
     *
     * @return 主队得分减客队得分
     */
    public int diff90() {
        return homeScore90Safe() - awayScore90Safe();
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Integer getHomeScore90() {
        return homeScore90;
    }

    public void setHomeScore90(Integer homeScore90) {
        this.homeScore90 = homeScore90;
    }

    public Integer getAwayScore90() {
        return awayScore90;
    }

    public void setAwayScore90(Integer awayScore90) {
        this.awayScore90 = awayScore90;
    }

    public Integer getHomeScoreExtra() {
        return homeScoreExtra;
    }

    public void setHomeScoreExtra(Integer homeScoreExtra) {
        this.homeScoreExtra = homeScoreExtra;
    }

    public Integer getAwayScoreExtra() {
        return awayScoreExtra;
    }

    public void setAwayScoreExtra(Integer awayScoreExtra) {
        this.awayScoreExtra = awayScoreExtra;
    }

    public Integer getHomeScorePenalty() {
        return homeScorePenalty;
    }

    public void setHomeScorePenalty(Integer homeScorePenalty) {
        this.homeScorePenalty = homeScorePenalty;
    }

    public Integer getAwayScorePenalty() {
        return awayScorePenalty;
    }

    public void setAwayScorePenalty(Integer awayScorePenalty) {
        this.awayScorePenalty = awayScorePenalty;
    }

    public Integer getHomeScoreTotal() {
        return homeScoreTotal;
    }

    public void setHomeScoreTotal(Integer homeScoreTotal) {
        this.homeScoreTotal = homeScoreTotal;
    }

    public Integer getAwayScoreTotal() {
        return awayScoreTotal;
    }

    public void setAwayScoreTotal(Integer awayScoreTotal) {
        this.awayScoreTotal = awayScoreTotal;
    }
}
