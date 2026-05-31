package com.eugene.goalhub.match.service;

import dto.*;

import java.util.List;

/**
 * 后台比赛国际化配置管理服务。
 */
public interface AdminMatchI18nService {

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    List<MatchI18nResponse> listMatchI18n(MatchI18nListRequest request);

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     */
    void addMatchI18n(AddMatchI18nRequest request);

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     */
    void updateMatchI18n(UpdateMatchI18nRequest request);

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     */
    void deleteMatchI18n(DeleteMatchI18nRequest request);
}
