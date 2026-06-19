package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.mapper.AppChampionOddsMapper;
import com.eugene.goalhub.match.service.AppChampionOddsService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.AppChampionOddsPageRequest;
import dto.AppChampionOddsResponse;
import dto.PageResponse;
import org.springframework.stereotype.Service;

/**
 * App 冠军赔率查询服务实现。
 *
 * <p>负责初始化分页和语言参数，并查询前端可见且可下注的冠军赔率。</p>
 */
@Service
public class AppChampionOddsServiceImpl implements AppChampionOddsService {

    /**
     * 日志模块名称。
     */
    private static final String MODULE_NAME = "前端冠军赔率查询";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 默认语言编码。
     */
    private static final String DEFAULT_LANG_CODE = "zh-CN";

    /**
     * App 冠军赔率查询 Mapper。
     */
    private final AppChampionOddsMapper appChampionOddsMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建 App 冠军赔率查询服务实现。
     *
     * @param appChampionOddsMapper App 冠军赔率查询 Mapper
     * @param matchOperationLogger  比赛服务操作日志工具
     */
    public AppChampionOddsServiceImpl(
            AppChampionOddsMapper appChampionOddsMapper,
            MatchOperationLogger matchOperationLogger) {
        this.appChampionOddsMapper = appChampionOddsMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询前端可见且可下注的冠军赔率。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    @Override
    public PageResponse<AppChampionOddsResponse> pageChampionOdds(
            AppChampionOddsPageRequest request) {
        if (request == null) {
            request = new AppChampionOddsPageRequest();
        }

        initPage(request);
        initLangCode(request);

        Page<AppChampionOddsResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AppChampionOddsResponse> result =
                appChampionOddsMapper.pageChampionOdds(page, request);

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "PAGE_CHAMPION_ODDS",
                "分页查询前端冠军赔率，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", leagueId=" + request.getLeagueId()
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

    /**
     * 初始化分页参数。
     *
     * @param request 冠军赔率分页查询参数
     */
    private void initPage(
            AppChampionOddsPageRequest request) {
        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
            return;
        }

        if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 初始化语言编码。
     *
     * @param request 冠军赔率分页查询参数
     */
    private void initLangCode(
            AppChampionOddsPageRequest request) {
        if (request.getLangCode() == null || request.getLangCode().isBlank()) {
            request.setLangCode(DEFAULT_LANG_CODE);
        }
    }
}
