package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminContentFeignClient;
import com.eugene.goalhub.admin.service.AdminContentService;
import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import org.springframework.stereotype.Service;
import response.Result;

@Service
public class AdminContentServiceImpl implements AdminContentService {

    private final AdminContentFeignClient adminContentFeignClient;

    public AdminContentServiceImpl(AdminContentFeignClient adminContentFeignClient) {
        this.adminContentFeignClient = adminContentFeignClient;
    }

    @Override
    public Result<Long> create(AdminContentCreateRequest request) {
        return adminContentFeignClient.create(request);
    }

    @Override
    public Result<Void> update(Long id, AdminContentUpdateRequest request) {
        return adminContentFeignClient.update(id, request);
    }

    @Override
    public Result<Void> delete(Long id) {
        return adminContentFeignClient.delete(id);
    }

    @Override
    public Result<ContentResponse> detail(Long id) {
        return adminContentFeignClient.detail(id);
    }

    @Override
    public Result<PageResponse<ContentResponse>> page(AdminContentPageRequest request) {
        return adminContentFeignClient.page(request);
    }
}