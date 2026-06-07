package com.eugene.goalhub.admin.client;

import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;
import response.Result;

@FeignClient(
        name = "match-service",
        contextId = "adminContentFeignClient"
)
public interface AdminContentFeignClient {

    @PostMapping("/internal/contents")
    Result<Long> create(@RequestBody AdminContentCreateRequest request);

    @PutMapping("/internal/contents/{id}")
    Result<Void> update(@PathVariable("id") Long id,
                        @RequestBody AdminContentUpdateRequest request);

    @DeleteMapping("/internal/contents/{id}")
    Result<Void> delete(@PathVariable("id") Long id);

    @GetMapping("/internal/contents/{id}")
    Result<ContentResponse> detail(@PathVariable("id") Long id);

    @PostMapping("/internal/contents/page")
    Result<PageResponse<ContentResponse>> page(@RequestBody AdminContentPageRequest request);
}