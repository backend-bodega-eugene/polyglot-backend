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
import dto.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 后台赛事基础数据管理服务实现。
 */
@Service
public class AdminMatchServiceImpl implements AdminMatchService {

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
                                 SoccerTeamMapper soccerTeamMapper) {
        this.adminMatchMapper = adminMatchMapper;
        this.soccerLeagueMapper = soccerLeagueMapper;
        this.soccerMatchMapper = soccerMatchMapper;
        this.soccerTeamMapper = soccerTeamMapper;
    }

    /**
     * 分页查询联赛。
     *
     * @param request 联赛分页查询条件
     * @return 联赛分页数据
     */
    @Override
    public PageResponse<AdminLeagueResponse> leaguePage(LeaguePageRequest request) {
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

        return response;
    }

    /**
     * 新增联赛。
     *
     * @param request 联赛新增参数
     */
    @Override
    public void addLeague(AddLeagueRequest request) {
        SoccerLeagueEntity entity = new SoccerLeagueEntity();
        entity.setCode(request.getCode());
        entity.setHostCountry(request.getHostCountry());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        soccerLeagueMapper.insert(entity);
    }

    /**
     * 更新联赛。
     *
     * @param request 联赛更新参数
     */
    @Override
    public void updateLeague(UpdateLeagueRequest request) {
        SoccerLeagueEntity entity = new SoccerLeagueEntity();
        entity.setId(request.getId());
        entity.setCode(request.getCode());
        entity.setHostCountry(request.getHostCountry());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        soccerLeagueMapper.updateById(entity);
    }

    /**
     * 删除联赛。
     *
     * @param request 联赛删除参数
     */
    @Override
    public void deleteLeague(DeleteLeagueRequest request) {
        soccerLeagueMapper.deleteById(request.getId());
    }

    /**
     * 分页查询比赛。
     *
     * @param request 比赛分页查询条件
     * @return 比赛分页数据
     */
    @Override
    public PageResponse<AdminMatchResponse> matchPage(MatchPageRequest request) {
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

        return response;
    }

    /**
     * 新增比赛。
     *
     * @param request 比赛新增参数
     */
    @Override
    public void addMatch(AddMatchRequest request) {
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
    }

    /**
     * 更新比赛。
     *
     * @param request 比赛更新参数
     */
    @Override
    public void updateMatch(UpdateMatchRequest request) {
        SoccerMatchEntity entity = new SoccerMatchEntity();
        entity.setId(request.getId());
        entity.setLeagueId(request.getLeagueId());
        entity.setMatchCode(request.getMatchCode());
        entity.setStageCode(request.getStageCode());
        entity.setHomeTeamId(request.getHomeTeamId());
        entity.setAwayTeamId(request.getAwayTeamId());
        entity.setScheduledStartTimeUtc(request.getScheduledStartTimeUtc());
        entity.setHostCountry(request.getHostCountry());
        entity.setStatus(request.getStatus());

        soccerMatchMapper.updateById(entity);
    }

    /**
     * 删除比赛。
     *
     * @param request 比赛删除参数
     */
    @Override
    public void deleteMatch(DeleteMatchRequest request) {
        soccerMatchMapper.deleteById(request.getId());
    }

    /**
     * 初始化联赛分页参数。
     *
     * @param request 联赛分页查询条件
     */
    private void initPage(LeaguePageRequest request) {
        if (request.getPageIndex() == null) {
            request.setPageIndex(1);
        }
        if (request.getPageSize() == null) {
            request.setPageSize(20);
        }
    }

    /**
     * 分页查询球队。
     *
     * @param request 球队分页查询条件
     * @return 球队分页数据
     */
    @Override
    public PageResponse<AdminTeamResponse> teamPage(TeamPageRequest request) {
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

        return response;
    }

    /**
     * 新增球队。
     *
     * @param request 球队新增参数
     */
    @Override
    public void addTeam(AddTeamRequest request) {
        SoccerTeamEntity entity = new SoccerTeamEntity();
        entity.setCode(request.getCode());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        soccerTeamMapper.insert(entity);
    }

    /**
     * 更新球队。
     *
     * @param request 球队更新参数
     */
    @Override
    public void updateTeam(UpdateTeamRequest request) {
        SoccerTeamEntity entity = new SoccerTeamEntity();
        entity.setId(request.getId());
        entity.setCode(request.getCode());
        entity.setLogoUrl(request.getLogoUrl());
        entity.setStatus(request.getStatus());

        soccerTeamMapper.updateById(entity);
    }

    /**
     * 删除球队。
     *
     * @param request 球队删除参数
     */
    @Override
    public void deleteTeam(DeleteTeamRequest request) {
        soccerTeamMapper.deleteById(request.getId());
    }

    /**
     * 初始化球队分页参数。
     *
     * @param request 球队分页查询条件
     */
    private void initPage(TeamPageRequest request) {
        if (request.getPageIndex() == null) {
            request.setPageIndex(1);
        }

        if (request.getPageSize() == null) {
            request.setPageSize(20);
        }
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
        if (request.getPageIndex() == null) {
            request.setPageIndex(1);
        }
        if (request.getPageSize() == null) {
            request.setPageSize(20);
        }
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
}
