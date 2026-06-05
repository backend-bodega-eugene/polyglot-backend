package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.BetMarketEntity;
import com.eugene.goalhub.match.entity.BetMarketOptionEntity;
import com.eugene.goalhub.match.entity.MatchMarketOptionEntity;
import com.eugene.goalhub.match.mapper.BetMarketMapper;
import com.eugene.goalhub.match.mapper.BetMarketOptionMapper;
import com.eugene.goalhub.match.mapper.MatchMarketOptionMapper;
import com.eugene.goalhub.match.mapper.SoccerMatchMapper;
import com.eugene.goalhub.match.service.MatchMarketOptionService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.math.BigDecimal;

/**
 * 比赛投注选项管理服务实现。
 *
 * <p>负责后台赛事玩法赔率的分页查询、新增、更新、删除和默认值处理。</p>
 */
@Service
public class MatchMarketOptionServiceImpl implements MatchMarketOptionService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "比赛投注选项管理";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 比赛投注选项 Mapper。
     */
    private final MatchMarketOptionMapper matchMarketOptionMapper;

    /**
     * 比赛 Mapper。
     */
    private final SoccerMatchMapper soccerMatchMapper;

    /**
     * 投注玩法 Mapper。
     */
    private final BetMarketMapper betMarketMapper;

    /**
     * 投注玩法选项 Mapper。
     */
    private final BetMarketOptionMapper betMarketOptionMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建比赛投注选项管理服务实现。
     *
     * @param matchMarketOptionMapper 比赛投注选项 Mapper
     * @param betMarketMapper         投注玩法 Mapper
     * @param betMarketOptionMapper   投注玩法选项 Mapper
     */
    public MatchMarketOptionServiceImpl(
            MatchMarketOptionMapper matchMarketOptionMapper,
            SoccerMatchMapper soccerMatchMapper,
            BetMarketMapper betMarketMapper,
            BetMarketOptionMapper betMarketOptionMapper,
            MatchOperationLogger matchOperationLogger) {
        this.matchMarketOptionMapper = matchMarketOptionMapper;
        this.soccerMatchMapper = soccerMatchMapper;
        this.betMarketMapper = betMarketMapper;
        this.betMarketOptionMapper = betMarketOptionMapper;
        this.matchOperationLogger = matchOperationLogger;
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
        if (request == null) {
            request = new MatchMarketOptionPageRequest();
        }
        initPage(request);

        Page<MatchMarketOptionResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<MatchMarketOptionResponse> result =
                matchMarketOptionMapper.adminPage(page, request);

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "MATCH_MARKET_OPTION_PAGE",
                "分页查询比赛投注选项，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", matchId=" + request.getMatchId()
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
     * 新增比赛投注选项。
     *
     * @param request 比赛投注选项新增参数
     */
    @Override
    public void add(
            AddMatchMarketOptionRequest request) {
        requireRequest(request);
        requireId(request.getMatchId());
        requireId(request.getMarketId());
        requireId(request.getMarketOptionId());
        requireMatchExists(request.getMatchId());

        BetMarketEntity market =
                betMarketMapper.selectById(request.getMarketId());

        if (market == null) {
            throw new BusinessException(ResultCode.BET_MARKET_NOT_FOUND);
        }

        BetMarketOptionEntity marketOption =
                betMarketOptionMapper.selectById(request.getMarketOptionId());

        if (marketOption == null) {
            throw new BusinessException(ResultCode.BET_MARKET_OPTION_NOT_FOUND);
        }

        if (!request.getMarketId().equals(marketOption.getMarketId())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long count = matchMarketOptionMapper.selectCount(
                Wrappers.lambdaQuery(MatchMarketOptionEntity.class)
                        .eq(MatchMarketOptionEntity::getMatchId, request.getMatchId())
                        .eq(MatchMarketOptionEntity::getMarketId, request.getMarketId())
                        .eq(MatchMarketOptionEntity::getMarketOptionId, request.getMarketOptionId())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
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
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_MATCH_MARKET_OPTION",
                "新增比赛投注选项成功，matchMarketOptionId=" + entity.getId()
                        + ", matchId=" + entity.getMatchId()
                        + ", marketId=" + entity.getMarketId()
                        + ", marketOptionId=" + entity.getMarketOptionId()
        );
    }

    /**
     * 更新比赛投注选项。
     *
     * @param request 比赛投注选项更新参数
     */
    @Override
    public void update(
            UpdateMatchMarketOptionRequest request) {
        requireRequest(request);
        requireId(request.getId());

        MatchMarketOptionEntity entity =
                matchMarketOptionMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.MATCH_MARKET_OPTION_NOT_FOUND);
        }

        if (request.getOdds() != null) {
            validateOdds(request.getOdds());
            entity.setOdds(request.getOdds());
        }

        if (request.getVisible() != null) {
            validateVisible(request.getVisible());
            entity.setVisible(request.getVisible());
        }

        if (request.getBetStatus() != null && !request.getBetStatus().isBlank()) {
            entity.setBetStatus(request.getBetStatus());
        }

        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }

        matchMarketOptionMapper.updateById(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_MATCH_MARKET_OPTION",
                "更新比赛投注选项成功，matchMarketOptionId=" + entity.getId()
                        + ", matchId=" + entity.getMatchId()
                        + ", odds=" + entity.getOdds()
                        + ", betStatus=" + entity.getBetStatus()
        );
    }

    /**
     * 删除比赛投注选项。
     *
     * @param request 比赛投注选项删除参数
     */
    @Override
    public void delete(
            DeleteMatchMarketOptionRequest request) {
        requireRequest(request);
        requireId(request.getId());

        MatchMarketOptionEntity entity =
                matchMarketOptionMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.MATCH_MARKET_OPTION_NOT_FOUND);
        }

        matchMarketOptionMapper.deleteById(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_MATCH_MARKET_OPTION",
                "删除比赛投注选项成功，matchMarketOptionId=" + request.getId()
                        + ", matchId=" + entity.getMatchId()
        );
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
            throw new BusinessException(ResultCode.BET_ODDS_INVALID);
        }

        validateOdds(odds);
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

        validateVisible(visible);
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

    /**
     * 初始化分页参数。
     */
    private void initPage(MatchMarketOptionPageRequest request) {
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
     * 校验请求不能为空。
     */
    private void requireRequest(Object request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验 ID 不能为空。
     */
    private void requireId(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验比赛存在。
     */
    private void requireMatchExists(Long matchId) {
        if (soccerMatchMapper.selectById(matchId) == null) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }

    /**
     * 校验赔率必须大于 0。
     */
    private void validateOdds(BigDecimal odds) {
        if (odds.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BET_ODDS_INVALID);
        }
    }

    /**
     * 校验可见值。
     */
    private void validateVisible(Integer visible) {
        if (visible != 0 && visible != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
