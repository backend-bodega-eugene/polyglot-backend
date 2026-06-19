package com.eugene.goalhub.match.service;

import dto.AppChampionOddsPageRequest;
import dto.AppChampionOddsResponse;
import dto.PageResponse;

/**
 * App 冠军赔率查询服务。
 */
public interface AppChampionOddsService {

    /**
     * 分页查询前端可见且可下注的冠军赔率。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    PageResponse<AppChampionOddsResponse> pageChampionOdds(
            AppChampionOddsPageRequest request);
}
