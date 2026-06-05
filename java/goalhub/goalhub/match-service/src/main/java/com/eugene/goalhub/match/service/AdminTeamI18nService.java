package com.eugene.goalhub.match.service;

import dto.*;

import java.util.List;

/**
 * 后台球队国际化配置管理服务。
 *
 * <p>负责球队多语言配置的查询、新增、更新和删除。</p>
 */
public interface AdminTeamI18nService {

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    List<TeamI18nResponse> listTeamI18n(TeamI18nListRequest request);

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     */
    void addTeamI18n(AddTeamI18nRequest request);

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     */
    void updateTeamI18n(UpdateTeamI18nRequest request);

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     */
    void deleteTeamI18n(DeleteTeamI18nRequest request);
}
