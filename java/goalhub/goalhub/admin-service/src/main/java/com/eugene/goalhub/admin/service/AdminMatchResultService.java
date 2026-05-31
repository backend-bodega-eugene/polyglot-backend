package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台比赛结果管理服务。
 */
public interface AdminMatchResultService {

    /**
     * 分页查询比赛结果。
     *
     * @param request 比赛结果分页查询条件
     * @return 比赛结果分页数据
     */
    PageResponse<AdminMatchResultResponse> page(
            AdminMatchResultPageRequest request);

    /**
     * 保存比赛结果。
     *
     * @param request 比赛结果保存参数
     */
    void save(
            SaveMatchResultRequest request);

    /**
     * 审核比赛结果。
     *
     * @param request 比赛结果审核参数
     */
    void approve(
            ApproveMatchResultRequest request);
}
