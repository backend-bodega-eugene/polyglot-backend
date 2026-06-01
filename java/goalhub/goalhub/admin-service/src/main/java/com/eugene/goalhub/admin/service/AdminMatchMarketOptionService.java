package com.eugene.goalhub.admin.service;

import dto.*;

/**
 * 后台赛事玩法赔率管理服务。
 */
public interface AdminMatchMarketOptionService {

    /**
     * 分页查询赛事玩法赔率。
     *
     * @param request 赛事玩法赔率分页查询条件
     * @return 赛事玩法赔率分页数据
     */
    PageResponse<MatchMarketOptionResponse> page(
            MatchMarketOptionPageRequest request);

    /**
     * 新增赛事玩法赔率。
     *
     * @param request 赛事玩法赔率新增参数
     */
    void add(
            AddMatchMarketOptionRequest request);

    /**
     * 更新赛事玩法赔率。
     *
     * @param request 赛事玩法赔率更新参数
     */
    void update(
            UpdateMatchMarketOptionRequest request);

    /**
     * 删除赛事玩法赔率。
     *
     * @param request 赛事玩法赔率删除参数
     */
    void delete(
            DeleteMatchMarketOptionRequest request);
}
