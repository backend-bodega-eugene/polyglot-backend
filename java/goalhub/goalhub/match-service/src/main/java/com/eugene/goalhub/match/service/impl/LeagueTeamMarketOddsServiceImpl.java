package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.LeagueTeamMarketOddsEntity;
import com.eugene.goalhub.match.mapper.LeagueTeamMarketOddsMapper;
import com.eugene.goalhub.match.service.LeagueTeamMarketOddsService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 联盟球队玩法赔率后台管理服务实现。
 *
 * <p>负责联盟、玩法、球队维度赔率的分页、新增、更新、删除和联赛球队查询。</p>
 */
@Service
public class LeagueTeamMarketOddsServiceImpl
        implements LeagueTeamMarketOddsService {

    /**
     * 日志模块名称。
     */
    private static final String MODULE_NAME = "联盟球队玩法赔率管理";

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
     * 联盟球队玩法赔率 Mapper。
     */
    private final LeagueTeamMarketOddsMapper leagueTeamMarketOddsMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建联盟球队玩法赔率后台管理服务实现。
     *
     * @param leagueTeamMarketOddsMapper 联盟球队玩法赔率 Mapper
     * @param matchOperationLogger       比赛服务操作日志工具
     */
    public LeagueTeamMarketOddsServiceImpl(
            LeagueTeamMarketOddsMapper leagueTeamMarketOddsMapper,
            MatchOperationLogger matchOperationLogger) {

        this.leagueTeamMarketOddsMapper =
                leagueTeamMarketOddsMapper;
        this.matchOperationLogger =
                matchOperationLogger;
    }

    /**
     * 分页查询联盟球队玩法赔率配置列表。
     *
     * @param request 联盟球队玩法赔率分页查询参数
     * @return 联盟球队玩法赔率分页结果
     */
    @Override
    public PageResponse<LeagueTeamMarketOddsResponse> page(
            LeagueTeamMarketOddsPageRequest request) {

        if (request == null) {
            request = new LeagueTeamMarketOddsPageRequest();
        }

        initPage(request);
        initLangCode(request);

        Page<LeagueTeamMarketOddsResponse> page =
                new Page<>(
                        request.getPageIndex(),
                        request.getPageSize()
                );

        Page<LeagueTeamMarketOddsResponse> result =
                leagueTeamMarketOddsMapper.adminPage(
                        page,
                        request
                );

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "LEAGUE_TEAM_MARKET_ODDS_PAGE",
                "分页查询联盟球队玩法赔率，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", leagueId=" + request.getLeagueId()
                        + ", playId=" + request.getPlayId()
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

        String langCode =
                defaultLangCode(request.getLangCode());

        requireLeagueExists(request.getLeagueId());

        List<ChampionLeagueTeamResponse> list =
                leagueTeamMarketOddsMapper.selectLeagueTeams(
                        request.getLeagueId(),
                        langCode
                );

//        matchOperationLogger.sysLog(
//                MODULE_NAME,
//                "LEAGUE_TEAM_MARKET_ODDS_TEAMS",
//                "查询联赛球队，leagueId=" + request.getLeagueId()
//                        + ", total=" + list.size()
//        );

        return list;
    }

    /**
     * 新增联盟球队玩法赔率配置。
     *
     * @param request 新增联盟球队玩法赔率参数
     */
    @Override
    public void add(
            AddLeagueTeamMarketOddsRequest request) {

        requireRequest(request);
        requireId(request.getLeagueId());
        requireId(request.getPlayId());
        requireText(request.getPlayCode());
        requireText(request.getPlayName());
        requireId(request.getTeamId());

        requireLeagueExists(request.getLeagueId());
        requireTeamExists(request.getTeamId());

        validateOdds(request.getOdds());

        Long count =
                leagueTeamMarketOddsMapper.selectCount(
                        Wrappers.lambdaQuery(LeagueTeamMarketOddsEntity.class)
                                .eq(LeagueTeamMarketOddsEntity::getLeagueId, request.getLeagueId())
                                .eq(LeagueTeamMarketOddsEntity::getPlayId, request.getPlayId())
                                .eq(LeagueTeamMarketOddsEntity::getTeamId, request.getTeamId())
                );

        if (count != null && count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        String langCode =
                defaultLangCode(request.getLangCode());

        String teamName =
                leagueTeamMarketOddsMapper.selectTeamName(
                        request.getTeamId(),
                        langCode
                );

        if (teamName == null || teamName.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        LeagueTeamMarketOddsEntity entity =
                new LeagueTeamMarketOddsEntity();

        entity.setLeagueId(request.getLeagueId());
        entity.setPlayId(request.getPlayId());
        entity.setPlayCode(request.getPlayCode());
        entity.setPlayName(request.getPlayName());
        entity.setTeamId(request.getTeamId());
        entity.setTeamNameSnapshot(teamName);
        entity.setOdds(request.getOdds());
        entity.setVisible(defaultVisible(request.getVisible()));
        entity.setBetStatus(defaultBetStatus(request.getBetStatus()));
        entity.setSortOrder(defaultSortOrder(request.getSortOrder()));

        leagueTeamMarketOddsMapper.insert(entity);

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_LEAGUE_TEAM_MARKET_ODDS",
                "新增联盟球队玩法赔率成功，id=" + entity.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", playId=" + entity.getPlayId()
                        + ", teamId=" + entity.getTeamId()
                        + ", odds=" + entity.getOdds()
        );
    }

    /**
     * 更新联盟球队玩法赔率配置。
     *
     * @param request 更新联盟球队玩法赔率参数
     */
    @Override
    public void update(
            UpdateLeagueTeamMarketOddsRequest request) {

        requireRequest(request);
        requireId(request.getId());

        LeagueTeamMarketOddsEntity entity =
                leagueTeamMarketOddsMapper.selectById(request.getId());

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

        if (request.getBetStatus() != null
                && !request.getBetStatus().isBlank()) {

            entity.setBetStatus(request.getBetStatus());
        }

        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }

        if (request.getTeamNameSnapshot() != null
                && !request.getTeamNameSnapshot().isBlank()) {

            entity.setTeamNameSnapshot(request.getTeamNameSnapshot());
        }

        leagueTeamMarketOddsMapper.updateById(entity);

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_LEAGUE_TEAM_MARKET_ODDS",
                "更新联盟球队玩法赔率成功，id=" + entity.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", playId=" + entity.getPlayId()
                        + ", teamId=" + entity.getTeamId()
                        + ", odds=" + entity.getOdds()
                        + ", betStatus=" + entity.getBetStatus()
        );
    }

    /**
     * 删除指定联盟球队玩法赔率配置。
     *
     * @param request 删除联盟球队玩法赔率参数
     */
    @Override
    public void delete(
            DeleteLeagueTeamMarketOddsRequest request) {

        requireRequest(request);
        requireId(request.getId());

        LeagueTeamMarketOddsEntity entity =
                leagueTeamMarketOddsMapper.selectById(request.getId());

        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        leagueTeamMarketOddsMapper.deleteById(request.getId());

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_LEAGUE_TEAM_MARKET_ODDS",
                "删除联盟球队玩法赔率成功，id=" + request.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", playId=" + entity.getPlayId()
                        + ", teamId=" + entity.getTeamId()
        );
    }

    /**
     * 初始化分页参数。
     *
     * @param request 联盟球队玩法赔率分页查询参数
     */
    private void initPage(
            LeagueTeamMarketOddsPageRequest request) {

        if (request.getPageIndex() == null
                || request.getPageIndex() < 1) {

            request.setPageIndex(DEFAULT_PAGE_INDEX);
        }

        if (request.getPageSize() == null
                || request.getPageSize() < 1) {

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
     * @param request 联盟球队玩法赔率分页查询参数
     */
    private void initLangCode(
            LeagueTeamMarketOddsPageRequest request) {

        if (request.getLangCode() == null
                || request.getLangCode().isBlank()) {

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
     * 校验文本不能为空。
     *
     * @param value 文本值
     */
    private void requireText(
            String value) {

        if (value == null || value.isBlank()) {
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

        Long count =
                leagueTeamMarketOddsMapper.countLeague(leagueId);

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

        Long count =
                leagueTeamMarketOddsMapper.countTeam(teamId);

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

        if (odds == null
                || odds.compareTo(BigDecimal.ZERO) <= 0) {

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
