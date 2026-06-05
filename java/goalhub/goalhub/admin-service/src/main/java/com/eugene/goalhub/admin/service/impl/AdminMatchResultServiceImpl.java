package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchResultFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchResultService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.*;
import org.springframework.stereotype.Service;

/**
 * 后台比赛结果管理服务实现。
 * <p>
 * 当前服务通过 Feign 调用 match-service 的内部比赛结果接口。
 * 比赛结果保存和审核规则由 match-service 负责。
 */
@Service
public class AdminMatchResultServiceImpl
        implements AdminMatchResultService {

    /**
     * 后台比赛结果远程调用客户端。
     */
    private final AdminMatchResultFeignClient
            adminMatchResultFeignClient;

    /**
     * 创建后台比赛结果管理服务实现。
     *
     * @param adminMatchResultFeignClient 后台比赛结果远程调用客户端
     */
    public AdminMatchResultServiceImpl(
            AdminMatchResultFeignClient adminMatchResultFeignClient) {
        this.adminMatchResultFeignClient =
                adminMatchResultFeignClient;
    }

    /**
     * 分页查询比赛结果。
     *
     * @param request 比赛结果分页查询条件
     * @return 比赛结果分页数据
     */
    @Override
    public PageResponse<AdminMatchResultResponse> page(
            AdminMatchResultPageRequest request) {

        return FeignResultSupport.data(adminMatchResultFeignClient.page(request));
    }

    /**
     * 保存比赛结果。
     *
     * @param request 比赛结果保存参数
     */
    @Override
    public void save(
            SaveMatchResultRequest request) {

        FeignResultSupport.checkSuccess(adminMatchResultFeignClient.save(request));
    }

    /**
     * 审核比赛结果。
     *
     * @param request 比赛结果审核参数
     */
    @Override
    public void approve(
            ApproveMatchResultRequest request) {

        FeignResultSupport.checkSuccess(adminMatchResultFeignClient.approve(request));
    }
}
