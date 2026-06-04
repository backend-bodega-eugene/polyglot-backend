package com.eugene.goalhub.match.service.impl;

import com.eugene.goalhub.match.mapper.AppMatchOddsMapper;
import com.eugene.goalhub.match.service.AppMatchOddsService;
import dto.AppMatchMarketOptionResponse;
import dto.AppMatchMarketResponse;
import dto.AppMatchOddsFlatResponse;
import dto.AppMatchOddsResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppMatchOddsServiceImpl implements AppMatchOddsService {

    private final AppMatchOddsMapper appMatchOddsMapper;

    public AppMatchOddsServiceImpl(
            AppMatchOddsMapper appMatchOddsMapper) {
        this.appMatchOddsMapper = appMatchOddsMapper;
    }

    @Override
    public AppMatchOddsResponse getMatchOdds(
            Long matchId) {

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

        return response;
    }
}