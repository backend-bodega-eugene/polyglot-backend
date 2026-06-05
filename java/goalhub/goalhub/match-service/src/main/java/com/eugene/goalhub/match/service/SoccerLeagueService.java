package com.eugene.goalhub.match.service;

import dto.SoccerLeagueResponse;

import java.util.List;

/**
 * 足球联赛查询服务。
 *
 * <p>负责前端可用联赛列表查询，并按语言返回本地化名称。</p>
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
