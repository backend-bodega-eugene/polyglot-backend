package com.eugene.goalhub.match.service;

import dto.*;

import java.util.List;

/**
 * 足球比赛查询服务。
 */
public interface SoccerMatchService {

    /**
     * 分页查询足球比赛。
     *
     * @param request 分页和筛选条件
     * @return 比赛分页结果
     */
    PageResponse<SoccerMatchListResponse> pageMatches(SoccerMatchPageRequest request);

    /**
     * 查询足球比赛详情。
     *
     * @param id      赛事 ID
     * @param request 详情查询参数
     * @return 赛事详情
     */
    SoccerMatchDetailResponse getMatchDetail(Long id, SoccerMatchDetailRequest request);

    /**
     * 检查赛事是否存在。
     *
     * @param matchId 赛事 ID
     * @return true 表示存在，false 表示不存在
     */
    boolean existsById(Long matchId);

    /**
     * 分页查询今日比赛。
     *
     * @param request 分页和筛选条件
     * @return 今日比赛分页结果
     */
    PageResponse<SoccerMatchListResponse> pageTodayMatches(SoccerMatchPageRequest request);

    /**
     * 分页查询即将开始的比赛。
     *
     * @param request 分页和筛选条件
     * @return 即将开始比赛分页结果
     */
    PageResponse<SoccerMatchListResponse> pageUpcomingMatches(SoccerMatchPageRequest request);

    /**
     * 分页查询已结束比赛。
     *
     * @param request 分页和筛选条件
     * @return 已结束比赛分页结果
     */
    PageResponse<SoccerMatchListResponse> pageFinishedMatches(SoccerMatchPageRequest request);

    /**
     * 查询热门比赛。
     *
     * @param request 热门比赛查询参数
     * @return 热门比赛列表
     */
    List<SoccerMatchListResponse> listHotMatches(SoccerHotMatchRequest request);
}
