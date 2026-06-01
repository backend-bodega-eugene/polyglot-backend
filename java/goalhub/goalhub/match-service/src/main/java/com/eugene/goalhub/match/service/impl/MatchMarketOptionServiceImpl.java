package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.BetMarketEntity;
import com.eugene.goalhub.match.entity.BetMarketOptionEntity;
import com.eugene.goalhub.match.entity.MatchMarketOptionEntity;
import com.eugene.goalhub.match.mapper.BetMarketMapper;
import com.eugene.goalhub.match.mapper.BetMarketOptionMapper;
import com.eugene.goalhub.match.mapper.MatchMarketOptionMapper;
import com.eugene.goalhub.match.service.MatchMarketOptionService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.math.BigDecimal;

/**
 * 比赛投注选项管理服务实现。
 */
@Service
public class MatchMarketOptionServiceImpl implements MatchMarketOptionService {

    /**
     * 比赛投注选项 Mapper。
     */
    private final MatchMarketOptionMapper matchMarketOptionMapper;

    /**
     * 投注玩法 Mapper。
     */
    private final BetMarketMapper betMarketMapper;

    /**
     * 投注玩法选项 Mapper。
     */
    private final BetMarketOptionMapper betMarketOptionMapper;

    /**
     * 创建比赛投注选项管理服务实现。
     *
     * @param matchMarketOptionMapper 比赛投注选项 Mapper
     * @param betMarketMapper         投注玩法 Mapper
     * @param betMarketOptionMapper   投注玩法选项 Mapper
     */
    public MatchMarketOptionServiceImpl(
            MatchMarketOptionMapper matchMarketOptionMapper,
            BetMarketMapper betMarketMapper,
            BetMarketOptionMapper betMarketOptionMapper) {
        this.matchMarketOptionMapper = matchMarketOptionMapper;
        this.betMarketMapper = betMarketMapper;
        this.betMarketOptionMapper = betMarketOptionMapper;
    }

    /**
     * 分页查询比赛投注选项。
     *
     * @param request 比赛投注选项分页查询条件
     * @return 比赛投注选项分页数据
     */
    @Override
    public PageResponse<MatchMarketOptionResponse> page(
            MatchMarketOptionPageRequest request) {

        Page<MatchMarketOptionResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<MatchMarketOptionResponse> result =
                matchMarketOptionMapper.adminPage(page, request);

        return new PageResponse<>(
                result.getTotal(),
                request.getPageIndex(),
                request.getPageSize(),
                result.getRecords()
        );
    }

    /**
     * 新增比赛投注选项。
     *
     * @param request 比赛投注选项新增参数
     */
    @Override
    public void add(
            AddMatchMarketOptionRequest request) {

        BetMarketEntity market =
                betMarketMapper.selectById(request.getMarketId());

        if (market == null) {
            throw new BusinessException(ResultCode.FAIL);
        }

        BetMarketOptionEntity marketOption =
                betMarketOptionMapper.selectById(request.getMarketOptionId());

        if (marketOption == null) {
            throw new BusinessException(ResultCode.FAIL);
        }

        if (!request.getMarketId().equals(marketOption.getMarketId())) {
            throw new BusinessException(ResultCode.FAIL);
        }

        Long count = matchMarketOptionMapper.selectCount(
                Wrappers.lambdaQuery(MatchMarketOptionEntity.class)
                        .eq(MatchMarketOptionEntity::getMatchId, request.getMatchId())
                        .eq(MatchMarketOptionEntity::getMarketId, request.getMarketId())
                        .eq(MatchMarketOptionEntity::getMarketOptionId, request.getMarketOptionId())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.FAIL);
        }

        MatchMarketOptionEntity entity = new MatchMarketOptionEntity();
        entity.setMatchId(request.getMatchId());

        entity.setMarketId(market.getId());
        entity.setMarketOptionId(marketOption.getId());

        entity.setMarketCode(market.getCode());
        entity.setMarketName(market.getName());
        entity.setMarketOptionCode(marketOption.getCode());
        entity.setMarketOptionName(marketOption.getName());

        entity.setOdds(defaultOdds(request.getOdds()));
        entity.setVisible(defaultVisible(request.getVisible()));
        entity.setBetStatus(defaultBetStatus(request.getBetStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        matchMarketOptionMapper.insert(entity);
    }

    /**
     * 更新比赛投注选项。
     *
     * @param request 比赛投注选项更新参数
     */
    @Override
    public void update(
            UpdateMatchMarketOptionRequest request) {

        MatchMarketOptionEntity entity =
                matchMarketOptionMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.FAIL);
        }

        if (request.getOdds() != null) {
            entity.setOdds(request.getOdds());
        }

        if (request.getVisible() != null) {
            entity.setVisible(request.getVisible());
        }

        if (request.getBetStatus() != null && !request.getBetStatus().isBlank()) {
            entity.setBetStatus(request.getBetStatus());
        }

        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }

        matchMarketOptionMapper.updateById(entity);
    }

    /**
     * 删除比赛投注选项。
     *
     * @param request 比赛投注选项删除参数
     */
    @Override
    public void delete(
            DeleteMatchMarketOptionRequest request) {

        MatchMarketOptionEntity entity =
                matchMarketOptionMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.MATCH_MARKET_OPTION_NOT_FOUND);
        }

        matchMarketOptionMapper.deleteById(request.getId());
    }

    /**
     * 获取默认赔率。
     *
     * @param odds 请求赔率
     * @return 非空赔率
     */
    private BigDecimal defaultOdds(
            BigDecimal odds) {

        if (odds == null) {
            return BigDecimal.ZERO;
        }

        return odds;
    }

    /**
     * 获取默认可见状态。
     *
     * @param visible 请求可见状态
     * @return 非空可见状态
     */
    private Integer defaultVisible(
            Integer visible) {

        if (visible == null) {
            return 1;
        }

        return visible;
    }

    /**
     * 获取默认投注状态。
     *
     * @param betStatus 请求投注状态
     * @return 非空投注状态
     */
    private String defaultBetStatus(
            String betStatus) {

        if (betStatus == null || betStatus.isBlank()) {
            return "OPEN";
        }

        return betStatus;
    }

    /**
     * 获取默认排序值。
     *
     * @param sortOrder 请求排序值
     * @return 非空排序值
     */
    private Integer defaultSortOrder(
            Integer sortOrder) {

        if (sortOrder == null) {
            return 0;
        }

        return sortOrder;
    }
}
