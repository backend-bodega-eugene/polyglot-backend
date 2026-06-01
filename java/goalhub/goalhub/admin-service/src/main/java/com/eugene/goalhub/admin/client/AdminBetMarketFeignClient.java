package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * match-service 内部管理端投注玩法 Feign 客户端。
 */
@FeignClient(
        name = "match-service",
        contextId = "adminBetMarketFeignClient"
)
public interface AdminBetMarketFeignClient {

    /**
     * 分页查询投注玩法。
     *
     * @param request 投注玩法分页查询条件
     * @return 投注玩法分页数据
     */
    @PostMapping("/internal/admin/betmarket/page")
    Result<PageResponse<BetMarketResponse>> betMarketPage(
            @RequestBody BetMarketPageRequest request);

    /**
     * 新增投注玩法。
     *
     * @param request 投注玩法新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/betmarket/add")
    Result<Void> addBetMarket(
            @RequestBody AddBetMarketRequest request);

    /**
     * 更新投注玩法。
     *
     * @param request 投注玩法更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/betmarket/update")
    Result<Void> updateBetMarket(
            @RequestBody UpdateBetMarketRequest request);

    /**
     * 删除投注玩法。
     *
     * @param request 投注玩法删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/betmarket/delete")
    Result<Void> deleteBetMarket(
            @RequestBody DeleteBetMarketRequest request);

    /**
     * 查询投注玩法选项列表。
     *
     * @param request 投注玩法选项查询条件
     * @return 投注玩法选项列表
     */
    @PostMapping("/internal/admin/betmarket/option/list")
    Result<java.util.List<BetMarketOptionResponse>> betMarketOptionList(
            @RequestBody BetMarketOptionListRequest request);

    /**
     * 新增投注玩法选项。
     *
     * @param request 投注玩法选项新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/betmarket/option/add")
    Result<Void> addBetMarketOption(
            @RequestBody AddBetMarketOptionRequest request);

    /**
     * 更新投注玩法选项。
     *
     * @param request 投注玩法选项更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/betmarket/option/update")
    Result<Void> updateBetMarketOption(
            @RequestBody UpdateBetMarketOptionRequest request);

    /**
     * 删除投注玩法选项。
     *
     * @param request 投注玩法选项删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/betmarket/option/delete")
    Result<Void> deleteBetMarketOption(
            @RequestBody DeleteBetMarketOptionRequest request);
}
