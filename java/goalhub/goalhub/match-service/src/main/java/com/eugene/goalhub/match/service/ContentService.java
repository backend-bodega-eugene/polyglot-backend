package com.eugene.goalhub.match.service;

import dto.*;

/**
 * 内容管理服务。
 *
 * <p>负责后台内容维护，以及 App 端文章、消息和让球教程查询。</p>
 */
public interface ContentService {

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    Long create(AdminContentCreateRequest request);

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     */
    void update(Long id, AdminContentUpdateRequest request);

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     */
    void delete(Long id);

    /**
     * 查询后台内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    ContentResponse getAdminDetail(Long id);

    /**
     * 分页查询后台内容。
     *
     * @param request 后台内容分页查询参数
     * @return 内容分页数据
     */
    PageResponse<ContentResponse> adminPage(AdminContentPageRequest request);

    /**
     * 查询 App 内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    ContentResponse getAppDetail(Long id);

    /**
     * 查询让球教程内容。
     *
     * @return 让球教程内容
     */
    ContentResponse getHandicapTutorial();

    /**
     * 分页查询 App 文章内容。
     *
     * @param request App 内容分页查询参数
     * @return 文章分页数据
     */
    PageResponse<ContentResponse> appArticlePage(AppContentPageRequest request);

    /**
     * 分页查询 App 消息内容。
     *
     * @param request App 内容分页查询参数
     * @return 消息分页数据
     */
    PageResponse<ContentResponse> appMessagePage(AppContentPageRequest request);
}
