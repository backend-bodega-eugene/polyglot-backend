package com.eugene.goalhub.match.service;

import dto.*;

import java.util.List;

/**
 * 后台联赛国际化配置管理服务。
 */
public interface AdminLeagueI18nService {

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    List<LeagueI18nResponse> listLeagueI18n(LeagueI18nListRequest request);

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     */
    void addLeagueI18n(AddLeagueI18nRequest request);

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     */
    void updateLeagueI18n(UpdateLeagueI18nRequest request);

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     */
    void deleteLeagueI18n(DeleteLeagueI18nRequest request);
}
