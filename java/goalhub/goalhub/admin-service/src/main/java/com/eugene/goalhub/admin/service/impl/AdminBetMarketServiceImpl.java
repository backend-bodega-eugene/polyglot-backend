package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminBetMarketFeignClient;
import com.eugene.goalhub.admin.service.AdminBetMarketService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台投注玩法管理服务实现。
 */
@Service
public class AdminBetMarketServiceImpl implements AdminBetMarketService {

    /**
     * 后台投注玩法远程调用客户端。
     */
    private final AdminBetMarketFeignClient adminBetMarketFeignClient;

    /**
     * 创建后台投注玩法管理服务实现。
     *
     * @param adminBetMarketFeignClient 后台投注玩法远程调用客户端
     */
    public AdminBetMarketServiceImpl(
            AdminBetMarketFeignClient adminBetMarketFeignClient) {
        this.adminBetMarketFeignClient = adminBetMarketFeignClient;
    }

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    @Override
    public PageResponse<BetMarketResponse> betMarketPage(
            BetMarketPageRequest request) {

        return FeignResultSupport.data(adminBetMarketFeignClient.betMarketPage(request));
    }

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     */
    @Override
    public void addBetMarket(
            AddBetMarketRequest request) {

        FeignResultSupport.checkSuccess(adminBetMarketFeignClient.addBetMarket(request));
    }

    /**
     * 更新投注玩法。
     *
     * @param request 投注玩法更新参数
     */
    @Override
    public void updateBetMarket(
            UpdateBetMarketRequest request) {

        FeignResultSupport.checkSuccess(adminBetMarketFeignClient.updateBetMarket(request));
    }

    /**
     * 删除投注玩法。
     *
     * @param request 投注玩法删除参数
     */
    @Override
    public void deleteBetMarket(
            DeleteBetMarketRequest request) {

        FeignResultSupport.checkSuccess(adminBetMarketFeignClient.deleteBetMarket(request));
    }

    /**
     * 查询投注玩法选项列表。
     *
     * @param request 投注玩法选项查询条件
     * @return 投注玩法选项列表
     */
    @Override
    public List<BetMarketOptionResponse> betMarketOptionList(
            BetMarketOptionListRequest request) {

        return FeignResultSupport.data(adminBetMarketFeignClient.betMarketOptionList(request));
    }

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     */
    @Override
    public void addBetMarketOption(
            AddBetMarketOptionRequest request) {

        FeignResultSupport.checkSuccess(adminBetMarketFeignClient.addBetMarketOption(request));
    }

    /**
     * 更新投注玩法选项。
     *
     * @param request 投注玩法选项更新参数
     */
    @Override
    public void updateBetMarketOption(
            UpdateBetMarketOptionRequest request) {

        FeignResultSupport.checkSuccess(adminBetMarketFeignClient.updateBetMarketOption(request));
    }

    /**
     * 删除投注玩法选项。
     *
     * @param request 投注玩法选项删除参数
     */
    @Override
    public void deleteBetMarketOption(
            DeleteBetMarketOptionRequest request) {

        FeignResultSupport.checkSuccess(adminBetMarketFeignClient.deleteBetMarketOption(request));
    }
}
