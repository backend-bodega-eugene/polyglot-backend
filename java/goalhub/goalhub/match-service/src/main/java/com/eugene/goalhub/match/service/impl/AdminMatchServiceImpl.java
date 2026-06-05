package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.SoccerLeagueEntity;
import com.eugene.goalhub.match.entity.SoccerMatchEntity;
import com.eugene.goalhub.match.entity.SoccerTeamEntity;
import com.eugene.goalhub.match.mapper.AdminMatchMapper;
import com.eugene.goalhub.match.mapper.SoccerLeagueMapper;
import com.eugene.goalhub.match.mapper.SoccerMatchMapper;
import com.eugene.goalhub.match.mapper.SoccerTeamMapper;
import com.eugene.goalhub.match.service.AdminMatchService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import response.ResultCode;

import java.util.Objects;
import java.util.Set;

/**
 * 后台赛事基础数据管理服务实现。
 *
 * <p>负责后台联赛、比赛和球队基础数据的分页查询、新增、更新和删除。</p>
 */
@Service
public class AdminMatchServiceImpl implements AdminMatchService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "后台赛事基础数据";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 合法比赛状态集合。
     */
    private static final Set<String> VALID_MATCH_STATUSES =
            Set.of("SCHEDULED", "NOT_STARTED", "LIVE", "FINISHED", "CANCELLED");

    /**
     * 后台赛事复合查询 Mapper。
     */
    private final AdminMatchMapper adminMatchMapper;

    /**
     * 联赛基础数据 Mapper。
     */
    private final SoccerLeagueMapper soccerLeagueMapper;

    /**
     * 比赛基础数据 Mapper。
     */
    private final SoccerMatchMapper soccerMatchMapper;

    /**
     * 球队基础数据 Mapper。
     */
    private final SoccerTeamMapper soccerTeamMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建后台赛事基础数据管理服务实现。
     *
     * @param adminMatchMapper   后台赛事复合查询 Mapper
     * @param soccerLeagueMapper 联赛基础数据 Mapper
     * @param soccerMatchMapper  比赛基础数据 Mapper
     * @param soccerTeamMapper   球队基础数据 Mapper
     */
    public AdminMatchServiceImpl(AdminMatchMapper adminMatchMapper,
                                 SoccerLeagueMapper soccerLeagueMapper,
                                 SoccerMatchMapper soccerMatchMapper,
                                 SoccerTeamMapper soccerTeamMapper,
                                 MatchOperationLogger matchOperationLogger) {
        this.adminMatchMapper = adminMatchMapper;
        this.soccerLeagueMapper = soccerLeagueMapper;
        this.soccerMatchMapper = soccerMatchMapper;
        this.soccerTeamMapper = soccerTeamMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询联赛。
     *
     * @param request 联赛分页查询条件
     * @return 联赛分页数据
     */
    @Override
    public PageResponse<AdminLeagueResponse> leaguePage(LeaguePageRequest request) {
        if (request == null) {
            request = new LeaguePageRequest();
        }
        initPage(request);
        initLang(request);

        Page<AdminLeagueResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AdminLeagueResponse> result =
                adminMatchMapper.selectLeaguePage(page, request);

        PageResponse<AdminLeagueResponse> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageIndex(request.getPageIndex());
        response.setPageSize(request.getPageSize());
        response.setRecords(result.getRecords());

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "LEAGUE_PAGE",
                "分页查询联赛，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", langCode=" + request.getLangCode()
                        + ", total=" + result.getTotal()
        );
        return response;
    }

    /**
     * 新增联赛。
     *
     * @param request 联赛新增参数
     */
    @Override
    public void addLeague(AddLeagueRequest request) {
        requireRequest(request);
        requireNotBlank(request.getCode());
        validateBinaryStatus(request.getStatus());
        checkLeagueCodeUnique(request.getCode(), null);

        SoccerLeagueEntity entity = new SoccerLeagueEntity();
        entity.setCode(request.getCode());
        entity.setHostCountry(request.getHostCountry());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        soccerLeagueMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_LEAGUE",
                "新增联赛成功，leagueId=" + entity.getId() + ", code=" + entity.getCode()
        );
    }

    /**
     * 更新联赛。
     *
     * @param request 联赛更新参数
     */
    @Override
    public void updateLeague(UpdateLeagueRequest request) {
        requireRequest(request);
        requireId(request.getId());
        requireNotBlank(request.getCode());
        validateBinaryStatus(request.getStatus());

        SoccerLeagueEntity entity = soccerLeagueMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
        checkLeagueCodeUnique(request.getCode(), request.getId());

        entity.setCode(request.getCode());
        entity.setHostCountry(request.getHostCountry());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        updateLeagueOrThrow(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_LEAGUE",
                "更新联赛成功，leagueId=" + request.getId() + ", code=" + request.getCode()
        );
    }

    /**
     * 删除联赛。
     *
     * @param request 联赛删除参数
     */
    @Override
    public void deleteLeague(DeleteLeagueRequest request) {
        requireRequest(request);
        requireId(request.getId());
        deleteLeagueOrThrow(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_LEAGUE",
                "删除联赛成功，leagueId=" + request.getId()
        );
    }

    /**
     * 分页查询比赛。
     *
     * @param request 比赛分页查询条件
     * @return 比赛分页数据
     */
    @Override
    public PageResponse<AdminMatchResponse> matchPage(MatchPageRequest request) {
        if (request == null) {
            request = new MatchPageRequest();
        }
        initPage(request);
        initLang(request);

        Page<AdminMatchResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AdminMatchResponse> result =
                adminMatchMapper.selectMatchPage(page, request);

        PageResponse<AdminMatchResponse> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageIndex(request.getPageIndex());
        response.setPageSize(request.getPageSize());
        response.setRecords(result.getRecords());

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "MATCH_PAGE",
                "分页查询比赛，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", langCode=" + request.getLangCode()
                        + ", total=" + result.getTotal()
        );
        return response;
    }

    /**
     * 新增比赛。
     *
     * @param request 比赛新增参数
     */
    @Override
    public void addMatch(AddMatchRequest request) {
        requireRequest(request);
        validateMatchRequest(request.getLeagueId(), request.getHomeTeamId(),
                request.getAwayTeamId(), request.getMatchCode(), request.getStatus(), null);

        SoccerMatchEntity entity = new SoccerMatchEntity();
        entity.setLeagueId(request.getLeagueId());
        entity.setMatchCode(request.getMatchCode());
        entity.setStageCode(request.getStageCode());
        entity.setHomeTeamId(request.getHomeTeamId());
        entity.setAwayTeamId(request.getAwayTeamId());
        entity.setScheduledStartTimeUtc(request.getScheduledStartTimeUtc());
        entity.setHostCountry(request.getHostCountry());
        entity.setStatus(request.getStatus());

        soccerMatchMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_MATCH",
                "新增比赛成功，matchId=" + entity.getId() + ", matchCode=" + entity.getMatchCode()
        );
    }

    /**
     * 更新比赛。
     *
     * @param request 比赛更新参数
     */
    @Override
    public void updateMatch(UpdateMatchRequest request) {
        requireRequest(request);
        requireId(request.getId());

        SoccerMatchEntity entity = soccerMatchMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
        validateMatchRequest(request.getLeagueId(), request.getHomeTeamId(),
                request.getAwayTeamId(), request.getMatchCode(), request.getStatus(), request.getId());

        entity.setLeagueId(request.getLeagueId());
        entity.setMatchCode(request.getMatchCode());
        entity.setStageCode(request.getStageCode());
        entity.setHomeTeamId(request.getHomeTeamId());
        entity.setAwayTeamId(request.getAwayTeamId());
        entity.setScheduledStartTimeUtc(request.getScheduledStartTimeUtc());
        entity.setHostCountry(request.getHostCountry());
        entity.setStatus(request.getStatus());

        updateMatchOrThrow(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_MATCH",
                "更新比赛成功，matchId=" + request.getId() + ", matchCode=" + request.getMatchCode()
        );
    }

    /**
     * 删除比赛。
     *
     * @param request 比赛删除参数
     */
    @Override
    public void deleteMatch(DeleteMatchRequest request) {
        requireRequest(request);
        requireId(request.getId());
        deleteMatchOrThrow(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_MATCH",
                "删除比赛成功，matchId=" + request.getId()
        );
    }

    /**
     * 初始化联赛分页参数。
     *
     * @param request 联赛分页查询条件
     */
    private void initPage(LeaguePageRequest request) {
        initPageValues(request::getPageIndex, request::setPageIndex,
                request::getPageSize, request::setPageSize);
    }

    /**
     * 分页查询球队。
     *
     * @param request 球队分页查询条件
     * @return 球队分页数据
     */
    @Override
    public PageResponse<AdminTeamResponse> teamPage(TeamPageRequest request) {
        if (request == null) {
            request = new TeamPageRequest();
        }
        initPage(request);
        initLang(request);

        Page<AdminTeamResponse> page =
                new Page<>(request.getPageIndex(), request.getPageSize());

        Page<AdminTeamResponse> result =
                soccerTeamMapper.selectTeamPage(page, request);

        PageResponse<AdminTeamResponse> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setPageIndex(request.getPageIndex());
        response.setPageSize(request.getPageSize());
        response.setRecords(result.getRecords());

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "TEAM_PAGE",
                "分页查询球队，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", langCode=" + request.getLangCode()
                        + ", total=" + result.getTotal()
        );
        return response;
    }

    /**
     * 新增球队。
     *
     * @param request 球队新增参数
     */
    @Override
    public void addTeam(AddTeamRequest request) {
        requireRequest(request);
        requireNotBlank(request.getCode());
        validateBinaryStatus(request.getStatus());
        checkTeamCodeUnique(request.getCode(), null);

        SoccerTeamEntity entity = new SoccerTeamEntity();
        entity.setCode(request.getCode());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        soccerTeamMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_TEAM",
                "新增球队成功，teamId=" + entity.getId() + ", code=" + entity.getCode()
        );
    }

    /**
     * 更新球队。
     *
     * @param request 球队更新参数
     */
    @Override
    public void updateTeam(UpdateTeamRequest request) {
        requireRequest(request);
        requireId(request.getId());
        requireNotBlank(request.getCode());
        validateBinaryStatus(request.getStatus());

        SoccerTeamEntity entity = soccerTeamMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
        checkTeamCodeUnique(request.getCode(), request.getId());

        entity.setCode(request.getCode());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        updateTeamOrThrow(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_TEAM",
                "更新球队成功，teamId=" + request.getId() + ", code=" + request.getCode()
        );
    }

    /**
     * 删除球队。
     *
     * @param request 球队删除参数
     */
    @Override
    public void deleteTeam(DeleteTeamRequest request) {
        requireRequest(request);
        requireId(request.getId());
        deleteTeamOrThrow(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_TEAM",
                "删除球队成功，teamId=" + request.getId()
        );
    }

    /**
     * 初始化球队分页参数。
     *
     * @param request 球队分页查询条件
     */
    private void initPage(TeamPageRequest request) {
        initPageValues(request::getPageIndex, request::setPageIndex,
                request::getPageSize, request::setPageSize);
    }

    /**
     * 初始化球队查询语言编码。
     *
     * @param request 球队分页查询条件
     */
    private void initLang(TeamPageRequest request) {
        if (!StringUtils.hasText(request.getLangCode())) {
            request.setLangCode("en-US");
        }
    }

    /**
     * 初始化比赛分页参数。
     *
     * @param request 比赛分页查询条件
     */
    private void initPage(MatchPageRequest request) {
        initPageValues(request::getPageIndex, request::setPageIndex,
                request::getPageSize, request::setPageSize);
    }

    /**
     * 初始化联赛查询语言编码。
     *
     * @param request 联赛分页查询条件
     */
    private void initLang(LeaguePageRequest request) {
        if (!StringUtils.hasText(request.getLangCode())) {
            request.setLangCode("en-US");
        }
    }

    /**
     * 初始化比赛查询语言编码。
     *
     * @param request 比赛分页查询条件
     */
    private void initLang(MatchPageRequest request) {
        if (!StringUtils.hasText(request.getLangCode())) {
            request.setLangCode("en-US");
        }
    }

    /**
     * 初始化分页参数。
     */
    private void initPageValues(java.util.function.Supplier<Integer> pageIndexGetter,
                                java.util.function.Consumer<Integer> pageIndexSetter,
                                java.util.function.Supplier<Integer> pageSizeGetter,
                                java.util.function.Consumer<Integer> pageSizeSetter) {
        Integer pageIndex = pageIndexGetter.get();
        if (pageIndex == null || pageIndex < 1) {
            pageIndexSetter.accept(DEFAULT_PAGE_INDEX);
        }

        Integer pageSize = pageSizeGetter.get();
        if (pageSize == null || pageSize < 1) {
            pageSizeSetter.accept(DEFAULT_PAGE_SIZE);
            return;
        }

        if (pageSize > DEFAULT_PAGE_SIZE) {
            pageSizeSetter.accept(DEFAULT_PAGE_SIZE);
        }
    }

    /**
     * 校验请求对象不能为空。
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
     * 校验文本不能为空。
     */
    private void requireNotBlank(String value) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验 0/1 状态值。
     */
    private void validateBinaryStatus(Integer status) {
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验比赛新增/更新参数。
     */
    private void validateMatchRequest(Long leagueId, Long homeTeamId, Long awayTeamId,
                                      String matchCode, String status, Long currentMatchId) {
        requireId(leagueId);
        requireId(homeTeamId);
        requireId(awayTeamId);
        requireNotBlank(matchCode);

        if (Objects.equals(homeTeamId, awayTeamId)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        if (soccerLeagueMapper.selectById(leagueId) == null
                || soccerTeamMapper.selectById(homeTeamId) == null
                || soccerTeamMapper.selectById(awayTeamId) == null) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }

        if (StringUtils.hasText(status) && !VALID_MATCH_STATUSES.contains(status)) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Long count = soccerMatchMapper.selectCount(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(SoccerMatchEntity.class)
                        .eq(SoccerMatchEntity::getMatchCode, matchCode)
                        .ne(currentMatchId != null, SoccerMatchEntity::getId, currentMatchId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验联赛编码唯一。
     */
    private void checkLeagueCodeUnique(String code, Long currentLeagueId) {
        Long count = soccerLeagueMapper.selectCount(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(SoccerLeagueEntity.class)
                        .eq(SoccerLeagueEntity::getCode, code)
                        .ne(currentLeagueId != null, SoccerLeagueEntity::getId, currentLeagueId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验球队编码唯一。
     */
    private void checkTeamCodeUnique(String code, Long currentTeamId) {
        Long count = soccerTeamMapper.selectCount(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(SoccerTeamEntity.class)
                        .eq(SoccerTeamEntity::getCode, code)
                        .ne(currentTeamId != null, SoccerTeamEntity::getId, currentTeamId)
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void updateLeagueOrThrow(SoccerLeagueEntity entity) {
        if (soccerLeagueMapper.updateById(entity) <= 0) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }

    private void updateMatchOrThrow(SoccerMatchEntity entity) {
        if (soccerMatchMapper.updateById(entity) <= 0) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }

    private void updateTeamOrThrow(SoccerTeamEntity entity) {
        if (soccerTeamMapper.updateById(entity) <= 0) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }

    private void deleteLeagueOrThrow(Long id) {
        if (soccerLeagueMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }

    private void deleteMatchOrThrow(Long id) {
        if (soccerMatchMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }

    private void deleteTeamOrThrow(Long id) {
        if (soccerTeamMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
    }
}
