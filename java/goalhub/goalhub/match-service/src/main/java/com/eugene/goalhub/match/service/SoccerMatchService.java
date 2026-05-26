package com.eugene.goalhub.match.service;

import dto.*;

import java.util.List;

public interface SoccerMatchService {

    PageResponse<SoccerMatchListResponse> pageMatches(SoccerMatchPageRequest request);

    SoccerMatchDetailResponse getMatchDetail(Long id, SoccerMatchDetailRequest request);
    /**
     * 检查赛事是否存在
     *
     * @param matchId 赛事ID
     * @return true=存在 false=不存在
     */
    boolean existsById(Long matchId);
    PageResponse<SoccerMatchListResponse> pageTodayMatches(SoccerMatchPageRequest request);

    PageResponse<SoccerMatchListResponse> pageUpcomingMatches(SoccerMatchPageRequest request);

    PageResponse<SoccerMatchListResponse> pageFinishedMatches(SoccerMatchPageRequest request);
    List<SoccerMatchListResponse> listHotMatches(SoccerHotMatchRequest request);
}