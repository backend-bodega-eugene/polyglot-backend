package com.eugene.goalhub.admin.service;

import dto.*;

import java.util.List;

/**
 * 后台投注玩法管理服务。
 */
public interface AdminBetMarketService {

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    PageResponse<BetMarketResponse> betMarketPage(
            BetMarketPageRequest request);

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     */
    void addBetMarket(
            AddBetMarketRequest request);

    /**
     * 更新投注玩法。
     *
     * @param request 投注玩法更新参数
     */
    void updateBetMarket(
            UpdateBetMarketRequest request);

    /**
     * 删除投注玩法。
     *
     * @param request 投注玩法删除参数
     */
    void deleteBetMarket(
            DeleteBetMarketRequest request);

    /**
     * 查询投注玩法选项列表。
     *
     * @param request 投注玩法选项查询条件
     * @return 投注玩法选项列表
     */
    List<BetMarketOptionResponse> betMarketOptionList(
            BetMarketOptionListRequest request);

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     */
    void addBetMarketOption(
            AddBetMarketOptionRequest request);

    /**
     * 更新投注玩法选项。
     *
     * @param request 投注玩法选项更新参数
     */
    void updateBetMarketOption(
            UpdateBetMarketOptionRequest request);

    /**
     * 删除投注玩法选项。
     *
     * @param request 投注玩法选项删除参数
     */
    void deleteBetMarketOption(
            DeleteBetMarketOptionRequest request);
}
