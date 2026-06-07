package com.eugene.goalhub.admin.service;

import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import response.Result;

public interface AdminContentService {

    Result<Long> create(AdminContentCreateRequest request);

    Result<Void> update(Long id, AdminContentUpdateRequest request);

    Result<Void> delete(Long id);

    Result<ContentResponse> detail(Long id);

    Result<PageResponse<ContentResponse>> page(AdminContentPageRequest request);
}