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

/**
 * 后台内容管理服务实现。
 *
 * <p>当前服务通过 Feign 调用 match-service 的内部内容管理接口。</p>
 */
@Service
public class AdminContentServiceImpl implements AdminContentService {

    /**
     * 后台内容远程调用客户端。
     */
    private final AdminContentFeignClient adminContentFeignClient;

    /**
     * 创建后台内容管理服务实现。
     *
     * @param adminContentFeignClient 后台内容远程调用客户端
     */
    public AdminContentServiceImpl(AdminContentFeignClient adminContentFeignClient) {
        this.adminContentFeignClient = adminContentFeignClient;
    }

    /**
     * 创建内容。
     *
     * @param request 内容创建参数
     * @return 新内容 ID
     */
    @Override
    public Result<Long> create(AdminContentCreateRequest request) {
        return adminContentFeignClient.create(request);
    }

    /**
     * 更新内容。
     *
     * @param id      内容 ID
     * @param request 内容更新参数
     * @return 空结果
     */
    @Override
    public Result<Void> update(Long id, AdminContentUpdateRequest request) {
        return adminContentFeignClient.update(id, request);
    }

    /**
     * 删除内容。
     *
     * @param id 内容 ID
     * @return 空结果
     */
    @Override
    public Result<Void> delete(Long id) {
        return adminContentFeignClient.delete(id);
    }

    /**
     * 查询内容详情。
     *
     * @param id 内容 ID
     * @return 内容详情
     */
    @Override
    public Result<ContentResponse> detail(Long id) {
        return adminContentFeignClient.detail(id);
    }

    /**
     * 分页查询内容。
     *
     * @param request 内容分页查询条件
     * @return 内容分页数据
     */
    @Override
    public Result<PageResponse<ContentResponse>> page(AdminContentPageRequest request) {
        return adminContentFeignClient.page(request);
    }
}
