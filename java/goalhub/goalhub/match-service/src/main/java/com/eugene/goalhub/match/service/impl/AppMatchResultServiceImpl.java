package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.mapper.AppMatchResultMapper;
import com.eugene.goalhub.match.service.AppMatchResultService;
import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import dto.PageResponse;
import org.springframework.stereotype.Service;

@Service
public class AppMatchResultServiceImpl implements AppMatchResultService {

    private final AppMatchResultMapper appMatchResultMapper;

    public AppMatchResultServiceImpl(
            AppMatchResultMapper appMatchResultMapper) {
        this.appMatchResultMapper = appMatchResultMapper;
    }

    @Override
    public PageResponse<AppMatchResultResponse> pageResult(
            AppMatchResultPageRequest request) {

        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(1);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(20);
        }

        if (request.getLangCode() == null || request.getLangCode().isBlank()) {
            request.setLangCode("zh-CN");
        }

        Page<AppMatchResultResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AppMatchResultResponse> result =
                appMatchResultMapper.pageResult(page, request);

        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                result.getRecords()
        );
    }
}