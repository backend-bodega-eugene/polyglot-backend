package com.eugene.goalhub.match.service;

import dto.SoccerLeagueResponse;

import java.util.List;

/**
 * 足球联赛查询服务。
 */
public interface SoccerLeagueService {

    /**
     * 查询启用状态的联赛列表。
     *
     * @param keyword  联赛名称、简称或编码关键字
     * @param langCode 语言编码
     * @return 联赛列表
     */
    List<SoccerLeagueResponse> listLeagues(String keyword, String langCode);
}
