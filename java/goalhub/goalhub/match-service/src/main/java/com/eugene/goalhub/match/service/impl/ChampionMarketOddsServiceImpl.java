package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.ChampionMarketOddsEntity;
import com.eugene.goalhub.match.mapper.ChampionMarketOddsMapper;
import com.eugene.goalhub.match.service.ChampionMarketOddsService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 冠军赔率后台管理与内部查询服务实现。
 *
 * <p>负责冠军赔率分页、新增、更新、删除、联赛球队查询和订单下单快照查询。</p>
 */
@Service
public class ChampionMarketOddsServiceImpl implements ChampionMarketOddsService {

    /**
     * 日志模块名称。
     */
    private static final String MODULE_NAME = "冠军赔率管理";

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
     * 默认下注状态。
     */
    private static final String DEFAULT_BET_STATUS = "OPEN";

    /**
     * 冠军赔率 Mapper。
     */
    private final ChampionMarketOddsMapper championMarketOddsMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建冠军赔率后台管理服务实现。
     *
     * @param championMarketOddsMapper 冠军赔率 Mapper
     * @param matchOperationLogger     比赛服务操作日志工具
     */
    public ChampionMarketOddsServiceImpl(
            ChampionMarketOddsMapper championMarketOddsMapper,
            MatchOperationLogger matchOperationLogger) {
        this.championMarketOddsMapper = championMarketOddsMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询冠军赔率配置列表。
     *
     * @param request 冠军赔率分页查询参数
     * @return 冠军赔率分页结果
     */
    @Override
    public PageResponse<ChampionMarketOddsResponse> page(
            ChampionMarketOddsPageRequest request) {
        if (request == null) {
            request = new ChampionMarketOddsPageRequest();
        }

        initPage(request);
        initLangCode(request);

        Page<ChampionMarketOddsResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<ChampionMarketOddsResponse> result =
                championMarketOddsMapper.adminPage(page, request);

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "CHAMPION_MARKET_ODDS_PAGE",
                "分页查询冠军赔率，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", leagueId=" + request.getLeagueId()
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
     * 查询指定联赛下出现过的球队。
     *
     * @param request 联赛球队查询参数
     * @return 联赛球队列表
     */
    @Override
    public List<ChampionLeagueTeamResponse> leagueTeams(
            ChampionLeagueTeamRequest request) {
        requireRequest(request);
        requireId(request.getLeagueId());
        String langCode = defaultLangCode(request.getLangCode());

        requireLeagueExists(request.getLeagueId());

        List<ChampionLeagueTeamResponse> list =
                championMarketOddsMapper.selectLeagueTeams(
                        request.getLeagueId(),
                        langCode
                );

//        matchOperationLogger.sysLog(
//                MODULE_NAME,
//                "CHAMPION_LEAGUE_TEAMS",
//                "查询联赛球队，leagueId=" + request.getLeagueId()
//                        + ", total=" + list.size()
//        );

        return list;
    }

    /**
     * 新增冠军赔率配置。
     *
     * @param request 新增冠军赔率参数
     */
    @Override
    public void add(
            AddChampionMarketOddsRequest request) {
        requireRequest(request);
        requireId(request.getLeagueId());
        requireId(request.getTeamId());
        requireLeagueExists(request.getLeagueId());
        requireTeamExists(request.getTeamId());

        validateOdds(request.getOdds());

        Long count = championMarketOddsMapper.selectCount(
                Wrappers.lambdaQuery(ChampionMarketOddsEntity.class)
                        .eq(ChampionMarketOddsEntity::getLeagueId, request.getLeagueId())
                        .eq(ChampionMarketOddsEntity::getTeamId, request.getTeamId())
        );

        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String langCode = defaultLangCode(request.getLangCode());
        String teamName = championMarketOddsMapper.selectTeamName(
                request.getTeamId(),
                langCode
        );

        if (teamName == null || teamName.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        ChampionMarketOddsEntity entity = new ChampionMarketOddsEntity();
        entity.setLeagueId(request.getLeagueId());
        entity.setTeamId(request.getTeamId());
        entity.setTeamNameSnapshot(teamName);
        entity.setOdds(request.getOdds());
        entity.setVisible(defaultVisible(request.getVisible()));
        entity.setBetStatus(defaultBetStatus(request.getBetStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        championMarketOddsMapper.insert(entity);

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_CHAMPION_MARKET_ODDS",
                "新增冠军赔率成功，id=" + entity.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", teamId=" + entity.getTeamId()
                        + ", odds=" + entity.getOdds()
        );
    }

    /**
     * 更新冠军赔率配置。
     *
     * @param request 更新冠军赔率参数
     */
    @Override
    public void update(
            UpdateChampionMarketOddsRequest request) {
        requireRequest(request);
        requireId(request.getId());

        ChampionMarketOddsEntity entity =
                championMarketOddsMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
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

        if (request.getTeamNameSnapshot() != null
                && !request.getTeamNameSnapshot().isBlank()) {
            entity.setTeamNameSnapshot(request.getTeamNameSnapshot());
        }

        championMarketOddsMapper.updateById(entity);

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_CHAMPION_MARKET_ODDS",
                "更新冠军赔率成功，id=" + entity.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", teamId=" + entity.getTeamId()
                        + ", odds=" + entity.getOdds()
                        + ", betStatus=" + entity.getBetStatus()
        );
    }

    /**
     * 删除指定冠军赔率配置。
     *
     * @param request 删除冠军赔率参数
     */
    @Override
    public void delete(
            DeleteChampionMarketOddsRequest request) {
        requireRequest(request);
        requireId(request.getId());

        ChampionMarketOddsEntity entity =
                championMarketOddsMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        championMarketOddsMapper.deleteById(request.getId());

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_CHAMPION_MARKET_ODDS",
                "删除冠军赔率成功，id=" + request.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", teamId=" + entity.getTeamId()
        );
    }

    /**
     * 查询冠军赔率快照。
     *
     * @param request 冠军赔率快照查询参数
     * @return 冠军赔率快照
     */
    @Override
    public ChampionOddsSnapshotResponse getSnapshot(
            ChampionOddsSnapshotRequest request) {
        requireRequest(request);
        requireId(request.getChampionOddsId());

        String langCode = defaultLangCode(request.getLangCode());

        ChampionOddsSnapshotResponse snapshot =
                championMarketOddsMapper.selectChampionOddsSnapshot(
                        request.getChampionOddsId(),
                        langCode
                );

        if (snapshot == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        return snapshot;
    }

    /**
     * 初始化分页参数。
     *
     * @param request 冠军赔率分页查询参数
     */
    private void initPage(
            ChampionMarketOddsPageRequest request) {
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
            ChampionMarketOddsPageRequest request) {
        if (request.getLangCode() == null || request.getLangCode().isBlank()) {
            request.setLangCode(DEFAULT_LANG_CODE);
        }
    }

    /**
     * 获取默认语言编码。
     *
     * @param langCode 原始语言编码
     * @return 处理后的语言编码
     */
    private String defaultLangCode(
            String langCode) {
        if (langCode == null || langCode.isBlank()) {
            return DEFAULT_LANG_CODE;
        }
        return langCode;
    }

    /**
     * 获取默认可见状态。
     *
     * @param visible 原始可见状态
     * @return 处理后的可见状态
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
     * 获取默认下注状态。
     *
     * @param betStatus 原始下注状态
     * @return 处理后的下注状态
     */
    private String defaultBetStatus(
            String betStatus) {
        if (betStatus == null || betStatus.isBlank()) {
            return DEFAULT_BET_STATUS;
        }
        return betStatus;
    }

    /**
     * 获取默认排序值。
     *
     * @param sortOrder 原始排序值
     * @return 处理后的排序值
     */
    private Integer defaultSortOrder(
            Integer sortOrder) {
        if (sortOrder == null) {
            return 0;
        }
        return sortOrder;
    }

    /**
     * 校验请求对象不能为空。
     *
     * @param request 请求对象
     */
    private void requireRequest(
            Object request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验 ID 不能为空。
     *
     * @param id ID
     */
    private void requireId(
            Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验联赛存在。
     *
     * @param leagueId 联赛或杯赛 ID
     */
    private void requireLeagueExists(
            Long leagueId) {
        Long count = championMarketOddsMapper.countLeague(leagueId);
        if (count == null || count <= 0) {
            throw new BusinessException(ResultCode.LEAGUE_NOT_EXISTS);
        }
    }

    /**
     * 校验球队存在。
     *
     * @param teamId 球队 ID
     */
    private void requireTeamExists(
            Long teamId) {
        Long count = championMarketOddsMapper.countTeam(teamId);
        if (count == null || count <= 0) {
            throw new BusinessException(ResultCode.TEAM_NOT_EXISTS);
        }
    }

    /**
     * 校验赔率有效。
     *
     * @param odds 赔率
     */
    private void validateOdds(
            BigDecimal odds) {
        if (odds == null || odds.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.BET_ODDS_INVALID);
        }
    }

    /**
     * 校验可见状态有效。
     *
     * @param visible 可见状态
     */
    private void validateVisible(
            Integer visible) {
        if (visible != 0 && visible != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
