package com.eugene.goalhub.match.service;

import dto.*;

import java.util.List;

/**
 * 投注玩法管理服务。
 */
public interface BetMarketService {

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    PageResponse<BetMarketResponse> page(
            BetMarketPageRequest request);

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     */
    void add(
            AddBetMarketRequest request);

    /**
     * 更新投注玩法。
     *
     * @param request 投注玩法更新参数
     */
    void update(
            UpdateBetMarketRequest request);

    /**
     * 删除投注玩法。
     *
     * @param request 投注玩法删除参数
     */
    void delete(
            DeleteBetMarketRequest request);

    /**
     * 查询投注玩法选项列表。
     *
     * @param request 投注玩法选项查询条件
     * @return 投注玩法选项列表
     */
    List<BetMarketOptionResponse> optionList(
            BetMarketOptionListRequest request);

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     */
    void addOption(
            AddBetMarketOptionRequest request);

    /**
     * 更新投注玩法选项。
     *
     * @param request 投注玩法选项更新参数
     */
    void updateOption(
            UpdateBetMarketOptionRequest request);

    /**
     * 删除投注玩法选项。
     *
     * @param request 投注玩法选项删除参数
     */
    void deleteOption(
            DeleteBetMarketOptionRequest request);
}
