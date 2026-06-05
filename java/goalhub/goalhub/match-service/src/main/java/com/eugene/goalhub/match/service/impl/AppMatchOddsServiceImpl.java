package com.eugene.goalhub.match.service.impl;

import com.eugene.goalhub.match.mapper.AppMatchOddsMapper;
import com.eugene.goalhub.match.service.AppMatchOddsService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.AppMatchMarketOptionResponse;
import dto.AppMatchMarketResponse;
import dto.AppMatchOddsFlatResponse;
import dto.AppMatchOddsResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 前端赛事赔率查询服务实现。
 *
 * <p>负责查询赛事赔率平铺数据，并按投注玩法聚合为前端展示结构。</p>
 */
@Service
public class AppMatchOddsServiceImpl implements AppMatchOddsService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "前端赛事赔率查询";

    /**
     * 前端赛事赔率 Mapper。
     */
    private final AppMatchOddsMapper appMatchOddsMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建前端赛事赔率查询服务实现。
     *
     * @param appMatchOddsMapper 前端赛事赔率 Mapper
     */
    public AppMatchOddsServiceImpl(
            AppMatchOddsMapper appMatchOddsMapper,
            MatchOperationLogger matchOperationLogger) {
        this.appMatchOddsMapper = appMatchOddsMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 查询指定赛事的赔率信息。
     *
     * @param matchId 赛事 ID
     * @return 赛事赔率聚合响应
     */
    @Override
    public AppMatchOddsResponse getMatchOdds(
            Long matchId) {
        if (matchId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        List<AppMatchOddsFlatResponse> rows =
                appMatchOddsMapper.listByMatchId(matchId);

        Map<Long, AppMatchMarketResponse> marketMap = new LinkedHashMap<>();

        for (AppMatchOddsFlatResponse row : rows) {
            AppMatchMarketResponse market = marketMap.get(row.getMarketId());

            if (market == null) {
                market = new AppMatchMarketResponse();
                market.setMarketId(row.getMarketId());
                market.setMarketCode(row.getMarketCode());
                market.setMarketName(row.getMarketName());
                market.setOptions(new ArrayList<>());

                marketMap.put(row.getMarketId(), market);
            }

            AppMatchMarketOptionResponse option = new AppMatchMarketOptionResponse();
            option.setId(row.getId());
            option.setMarketOptionId(row.getMarketOptionId());
            option.setMarketOptionCode(row.getMarketOptionCode());
            option.setMarketOptionName(row.getMarketOptionName());
            option.setOdds(row.getOdds());
            option.setBetStatus(row.getBetStatus());
            option.setSortOrder(row.getSortOrder());

            market.getOptions().add(option);
        }

        AppMatchOddsResponse response = new AppMatchOddsResponse();
        response.setMatchId(matchId);
        response.setMarkets(new ArrayList<>(marketMap.values()));

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "GET_MATCH_ODDS",
                "查询赛事赔率成功，matchId=" + matchId
                        + ", marketCount=" + response.getMarkets().size()
                        + ", optionCount=" + rows.size()
        );
        return response;
    }
}
