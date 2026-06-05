package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.mapper.AppMatchResultMapper;
import com.eugene.goalhub.match.service.AppMatchResultService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.AppMatchResultPageRequest;
import dto.AppMatchResultResponse;
import dto.PageResponse;
import org.springframework.stereotype.Service;

/**
 * 前端赛事赛果查询服务实现。
 *
 * <p>负责初始化分页和语言参数，并分页查询前端展示用的赛事赛果。</p>
 */
@Service
public class AppMatchResultServiceImpl implements AppMatchResultService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "前端赛事赛果查询";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 前端赛事赛果 Mapper。
     */
    private final AppMatchResultMapper appMatchResultMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建前端赛事赛果查询服务实现。
     *
     * @param appMatchResultMapper 前端赛事赛果 Mapper
     */
    public AppMatchResultServiceImpl(
            AppMatchResultMapper appMatchResultMapper,
            MatchOperationLogger matchOperationLogger) {
        this.appMatchResultMapper = appMatchResultMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询前端赛事赛果。
     *
     * @param request 赛事赛果分页查询条件
     * @return 赛事赛果分页响应
     */
    @Override
    public PageResponse<AppMatchResultResponse> pageResult(
            AppMatchResultPageRequest request) {
        if (request == null) {
            request = new AppMatchResultPageRequest();
        }

        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        } else if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
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

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "PAGE_MATCH_RESULT",
                "分页查询前端赛事赛果，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", langCode=" + request.getLangCode()
                        + ", total=" + result.getTotal()
        );
        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                result.getRecords()
        );
    }
}
