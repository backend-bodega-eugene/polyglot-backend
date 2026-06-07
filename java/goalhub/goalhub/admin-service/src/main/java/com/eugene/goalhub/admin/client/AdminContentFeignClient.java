package com.eugene.goalhub.admin.client;

import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import response.Result;

/**
 * match-service 内部管理端内容 Feign 客户端。
 */
@FeignClient(
        name = "match-service",
        contextId = "adminContentFeignClient"
)
public interface AdminContentFeignClient {

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    @PostMapping("/internal/contents")
    Result<Long> create(@RequestBody AdminContentCreateRequest request);

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     * @return 空结果
     */
    @PutMapping("/internal/contents/{id}")
    Result<Void> update(@PathVariable("id") Long id,
                        @RequestBody AdminContentUpdateRequest request);

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     * @return 空结果
     */
    @DeleteMapping("/internal/contents/{id}")
    Result<Void> delete(@PathVariable("id") Long id);

    /**
     * 查询内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    @GetMapping("/internal/contents/{id}")
    Result<ContentResponse> detail(@PathVariable("id") Long id);

    /**
     * 分页查询内容。
     *
     * @param request 内容分页查询条件
     * @return 内容分页数据
     */
    @PostMapping("/internal/contents/page")
    Result<PageResponse<ContentResponse>> page(@RequestBody AdminContentPageRequest request);
}
