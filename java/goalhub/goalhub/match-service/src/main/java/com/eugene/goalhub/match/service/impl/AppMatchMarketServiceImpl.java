package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.mapper.AppMatchMarketMapper;
import com.eugene.goalhub.match.service.AppMatchMarketService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * App 赛事玩法赔率聚合查询服务实现。
 *
 * <p>查询赛事玩法赔率扁平数据，并按联赛、比赛、玩法聚合为前端展示结构。</p>
 */
@Service
public class AppMatchMarketServiceImpl implements AppMatchMarketService {

    /**
     * 日志模块名称。
     */
    private static final String MODULE_NAME = "前端赛事玩法赔率聚合查询";

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
     * 数据库时间字符串格式化器。
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * App 赛事玩法赔率聚合查询 Mapper。
     */
    private final AppMatchMarketMapper appMatchMarketMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建 App 赛事玩法赔率聚合查询服务实现。
     *
     * @param appMatchMarketMapper App 赛事玩法赔率聚合查询 Mapper
     * @param matchOperationLogger 比赛服务操作日志工具
     */
    public AppMatchMarketServiceImpl(
            AppMatchMarketMapper appMatchMarketMapper,
            MatchOperationLogger matchOperationLogger) {
        this.appMatchMarketMapper = appMatchMarketMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询今日赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Override
    public PageResponse<AppMatchMarketLeagueResponse> pageToday(
            AppMatchMarketQueryRequest request) {

        LocalDate today = LocalDate.now();

        return pageByType(
                request,
                "TODAY",
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay().minusSeconds(1)
        );
    }

    /**
     * 分页查询滚球赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Override
    public PageResponse<AppMatchMarketLeagueResponse> pageLive(
            AppMatchMarketQueryRequest request) {

        return pageByType(request, "LIVE", null, null);
    }

    /**
     * 分页查询早盘赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Override
    public PageResponse<AppMatchMarketLeagueResponse> pageEarly(
            AppMatchMarketQueryRequest request) {

        LocalDate tomorrow = LocalDate.now().plusDays(1);

        return pageByType(
                request,
                "EARLY",
                tomorrow.atStartOfDay(),
                tomorrow.plusDays(2).atStartOfDay().minusSeconds(1)
        );
    }

    /**
     * 分页查询串关赛事玩法赔率。
     *
     * @param request 赛事玩法赔率聚合查询参数
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    @Override
    public PageResponse<AppMatchMarketLeagueResponse> pageParlay(
            AppMatchMarketQueryRequest request) {

        return pageByType(request, "PARLAY", null, null);
    }

    /**
     * 按查询类型分页查询赛事玩法赔率。
     *
     * @param request   赛事玩法赔率聚合查询参数
     * @param type      查询类型
     * @param startTime 查询开始时间
     * @param endTime   查询结束时间
     * @return 按联赛聚合的赛事玩法赔率分页结果
     */
    private PageResponse<AppMatchMarketLeagueResponse> pageByType(
            AppMatchMarketQueryRequest request,
            String type,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (request == null) {
            request = new AppMatchMarketQueryRequest();
        }

        initRequest(request);

        Page<AppMatchMarketFlatResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AppMatchMarketFlatResponse> result =
                appMatchMarketMapper.pageFlat(
                        page,
                        request,
                        type,
                        formatTime(startTime),
                        formatTime(endTime)
                );

        List<AppMatchMarketLeagueResponse> records =
                buildTree(result.getRecords());

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "PAGE_" + type + "_MATCH_MARKET",
                "分页查询赛事玩法赔率聚合，type=" + type
                        + ", pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", leagueId=" + request.getLeagueId()
                        + ", total=" + result.getTotal()
        );

        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                records
        );
    }

    /**
     * 将扁平查询结果聚合为联赛、比赛、玩法、选项树。
     *
     * @param rows 赛事玩法赔率扁平数据
     * @return 按联赛聚合的赛事玩法赔率列表
     */
    private List<AppMatchMarketLeagueResponse> buildTree(
            List<AppMatchMarketFlatResponse> rows) {

        Map<Long, AppMatchMarketLeagueResponse> leagueMap =
                new LinkedHashMap<>();

        Map<Long, AppMatchMarketMatchResponse> matchMap =
                new LinkedHashMap<>();

        Map<String, AppMatchMarketResponse> marketMap =
                new LinkedHashMap<>();

        for (AppMatchMarketFlatResponse row : rows) {
            AppMatchMarketLeagueResponse league =
                    leagueMap.get(row.getLeagueId());

            if (league == null) {
                league = new AppMatchMarketLeagueResponse();
                league.setLeagueId(row.getLeagueId());
                league.setLeagueName(row.getLeagueName());
                league.setLeagueLogoUrl(row.getLeagueLogoUrl());
                league.setMatches(new ArrayList<>());

                leagueMap.put(row.getLeagueId(), league);
            }

            AppMatchMarketMatchResponse match =
                    matchMap.get(row.getMatchId());

            if (match == null) {
                match = new AppMatchMarketMatchResponse();
                match.setMatchId(row.getMatchId());
                match.setMatchCode(row.getMatchCode());
                match.setMatchName(row.getMatchName());
                match.setMatchStatus(row.getMatchStatus());
                match.setScheduledStartTimeUtc(row.getScheduledStartTimeUtc());

                match.setHomeTeamId(row.getHomeTeamId());
                match.setHomeTeamName(row.getHomeTeamName());
                match.setHomeTeamLogoUrl(row.getHomeTeamLogoUrl());

                match.setAwayTeamId(row.getAwayTeamId());
                match.setAwayTeamName(row.getAwayTeamName());
                match.setAwayTeamLogoUrl(row.getAwayTeamLogoUrl());

                match.setMarkets(new ArrayList<>());

                league.getMatches().add(match);
                matchMap.put(row.getMatchId(), match);
            }

            String marketKey = row.getMatchId() + "_" + row.getMarketId();

            AppMatchMarketResponse market =
                    marketMap.get(marketKey);

            if (market == null) {
                market = new AppMatchMarketResponse();
                market.setMarketId(row.getMarketId());
                market.setMarketCode(row.getMarketCode());
                market.setMarketName(row.getMarketName());
                market.setOptions(new ArrayList<>());

                match.getMarkets().add(market);
                marketMap.put(marketKey, market);
            }

            AppMatchMarketOptionResponse option =
                    new AppMatchMarketOptionResponse();

            option.setId(row.getOptionId());
            option.setMarketOptionId(row.getMarketOptionId());
            option.setMarketOptionCode(row.getMarketOptionCode());
            option.setMarketOptionName(row.getMarketOptionName());
            option.setOdds(row.getOdds());
            option.setBetStatus(row.getBetStatus());
            option.setSortOrder(row.getSortOrder());

            market.getOptions().add(option);
        }

        return new ArrayList<>(leagueMap.values());
    }

    /**
     * 初始化查询参数。
     *
     * @param request 赛事玩法赔率聚合查询参数
     */
    private void initRequest(
            AppMatchMarketQueryRequest request) {

        if (request.getPageIndex() == null || request.getPageIndex() < 1) {
            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        } else if (request.getPageSize() > DEFAULT_PAGE_SIZE) {
            request.setPageSize(DEFAULT_PAGE_SIZE);
        }

        if (request.getLangCode() == null || request.getLangCode().isBlank()) {
            request.setLangCode(DEFAULT_LANG_CODE);
        }
    }

    /**
     * 格式化时间参数。
     *
     * @param time 时间
     * @return 格式化后的时间字符串
     */
    private String formatTime(
            LocalDateTime time) {

        if (time == null) {
            return null;
        }

        return FORMATTER.format(time);
    }
}
