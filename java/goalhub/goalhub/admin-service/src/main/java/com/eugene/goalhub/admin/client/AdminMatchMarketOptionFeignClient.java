package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * match-service 内部管理端比赛玩法选项 Feign 客户端。
 */
@FeignClient(
        name = "match-service",
        contextId = "adminMatchMarketOptionFeignClient"
)
public interface AdminMatchMarketOptionFeignClient {

    /**
     * 分页查询比赛玩法选项。
     *
     * @param request 比赛玩法选项分页查询条件
     * @return 比赛玩法选项分页数据
     */
    @PostMapping("/internal/admin/matchmarketoption/page")
    Result<PageResponse<MatchMarketOptionResponse>> page(
            @RequestBody MatchMarketOptionPageRequest request);

    /**
     * 新增比赛玩法选项。
     *
     * @param request 比赛玩法选项新增参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchmarketoption/add")
    Result<Void> add(
            @RequestBody AddMatchMarketOptionRequest request);

    /**
     * 更新比赛玩法选项。
     *
     * @param request 比赛玩法选项更新参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchmarketoption/update")
    Result<Void> update(
            @RequestBody UpdateMatchMarketOptionRequest request);

    /**
     * 删除比赛玩法选项。
     *
     * @param request 比赛玩法选项删除参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchmarketoption/delete")
    Result<Void> delete(
            @RequestBody DeleteMatchMarketOptionRequest request);
}
