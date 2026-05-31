package com.eugene.goalhub.admin.client;

import dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import response.Result;

/**
 * match-service 内部管理端比赛结果 Feign 客户端。
 */
@FeignClient(
        name = "match-service",
        contextId = "adminMatchResultFeignClient"
)
public interface AdminMatchResultFeignClient {

    /**
     * 分页查询比赛结果。
     *
     * @param request 比赛结果分页查询条件
     * @return 比赛结果分页数据
     */
    @PostMapping("/internal/admin/matchresult/page")
    Result<PageResponse<AdminMatchResultResponse>> page(
            @RequestBody AdminMatchResultPageRequest request);

    /**
     * 保存比赛结果。
     *
     * @param request 比赛结果保存参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchresult/save")
    Result<Void> save(
            @RequestBody SaveMatchResultRequest request);

    /**
     * 审核比赛结果。
     *
     * @param request 比赛结果审核参数
     * @return 空结果
     */
    @PostMapping("/internal/admin/matchresult/approve")
    Result<Void> approve(
            @RequestBody ApproveMatchResultRequest request);
}
