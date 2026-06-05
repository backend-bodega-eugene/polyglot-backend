package com.eugene.goalhub.match.service;

import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import dto.PageResponse;

/**
 * 前端赛事赛果查询服务。
 *
 * <p>负责按前端查询条件分页返回已审核赛事赛果。</p>
 */
public interface AppMatchResultService {

    /**
     * 分页查询前端赛事赛果。
     *
     * @param request 赛事赛果分页查询条件
     * @return 赛事赛果分页响应
     */
    PageResponse<AppMatchResultResponse> pageResult(
            AppMatchResultPageRequest request);
}
