package com.eugene.goalhub.match.service;

import dto.*;

/**
 * 比赛结果管理服务。
 *
 * <p>负责后台比赛结果分页查询、保存和审核。</p>
 */
public interface MatchResultService {

    /**
     * 分页查询后台比赛结果。
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
