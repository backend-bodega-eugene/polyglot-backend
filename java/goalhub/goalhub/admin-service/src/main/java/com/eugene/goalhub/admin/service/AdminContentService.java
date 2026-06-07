package com.eugene.goalhub.admin.service;

import dto.AdminContentCreateRequest;
import dto.AdminContentPageRequest;
import dto.AdminContentUpdateRequest;
import dto.ContentResponse;
import dto.PageResponse;
import response.Result;

/**
 * 后台内容管理服务。
 */
public interface AdminContentService {

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    Result<Long> create(AdminContentCreateRequest request);

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     * @return 空结果
     */
    Result<Void> update(Long id, AdminContentUpdateRequest request);

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     * @return 空结果
     */
    Result<Void> delete(Long id);

    /**
     * 查询内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    Result<ContentResponse> detail(Long id);

    /**
     * 分页查询内容。
     *
     * @param request 内容分页查询条件
     * @return 内容分页数据
     */
    Result<PageResponse<ContentResponse>> page(AdminContentPageRequest request);
}
