package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchResultFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchResultService;
import dto.*;
import org.springframework.stereotype.Service;
import response.Result;

@Service
public class AdminMatchResultServiceImpl
        implements AdminMatchResultService {

    private final AdminMatchResultFeignClient
            adminMatchResultFeignClient;

    public AdminMatchResultServiceImpl(
            AdminMatchResultFeignClient adminMatchResultFeignClient) {
        this.adminMatchResultFeignClient =
                adminMatchResultFeignClient;
    }

    @Override
    public PageResponse<AdminMatchResultResponse> page(
            AdminMatchResultPageRequest request) {

        Result<PageResponse<AdminMatchResultResponse>> result =
                adminMatchResultFeignClient.page(request);

        return result.getData();
    }

    @Override
    public void save(
            SaveMatchResultRequest request) {

        adminMatchResultFeignClient.save(request);
    }

    @Override
    public void approve(
            ApproveMatchResultRequest request) {

        adminMatchResultFeignClient.approve(request);
    }
}