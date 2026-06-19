package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.MatchResultEntity;
import com.eugene.goalhub.match.entity.SoccerMatchEntity;
import com.eugene.goalhub.match.mapper.MatchResultMapper;
import com.eugene.goalhub.match.mapper.SoccerMatchMapper;
import com.eugene.goalhub.match.service.MatchResultService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import response.ResultCode;

import java.time.LocalDateTime;

/**
 * 比赛结果管理服务实现。
 *
 * <p>负责后台比赛结果分页查询、赛果保存和赛果审核。</p>
 */
@Service
public class MatchResultServiceImpl implements MatchResultService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "比赛结果管理";

    /**
     * 默认页码。
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * 默认和最大每页数量。
     */
    private static final int DEFAULT_PAGE_SIZE = 100;

    /**
     * 已审核状态。
     */
    private static final int STATUS_APPROVED = 1;

    /**
     * 比赛已结束状态。
     */
    private static final String MATCH_STATUS_FINISHED = "FINISHED";

    /**
     * 比赛已取消状态。
     */
    private static final String MATCH_STATUS_CANCELLED = "CANCELLED";

    /**
     * 比赛结果 Mapper。
     */
    private final MatchResultMapper matchResultMapper;

    /**
     * 比赛 Mapper。
     */
    private final SoccerMatchMapper soccerMatchMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建比赛结果管理服务实现。
     *
     * @param matchResultMapper    比赛结果 Mapper
     * @param soccerMatchMapper    比赛 Mapper
     * @param matchOperationLogger 比赛服务操作日志工具
     */
    public MatchResultServiceImpl(
            MatchResultMapper matchResultMapper,
            SoccerMatchMapper soccerMatchMapper,
            MatchOperationLogger matchOperationLogger) {
        this.matchResultMapper = matchResultMapper;
        this.soccerMatchMapper = soccerMatchMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 分页查询后台比赛结果。
     *
     * @param request 比赛结果分页查询条件
     * @return 比赛结果分页数据
     */
    @Override
    public PageResponse<AdminMatchResultResponse> page(
            AdminMatchResultPageRequest request) {
        if (request == null) {
            request = new AdminMatchResultPageRequest();
        }
        initPage(request);

        Page<AdminMatchResultResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AdminMatchResultResponse> result =
                matchResultMapper.adminPage(page, request);

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "MATCH_RESULT_PAGE",
                "分页查询后台比赛结果，pageIndex=" + request.getPageIndex()
                        + ", pageSize=" + request.getPageSize()
                        + ", matchName=" + request.getMatchName()
                        + ", matchStatus=" + request.getMatchStatus()
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
     * 保存比赛结果。
     *
     * @param request 比赛结果保存参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(
            SaveMatchResultRequest request) {

        requireRequest(request);

        SoccerMatchEntity match =
                requireMatchExists(request.getMatchId());

        requireMatchStatusAllowApprove(match);
        requireResultScore(request);

        MatchResultEntity entity = matchResultMapper.selectOne(
                Wrappers.lambdaQuery(MatchResultEntity.class)
                        .eq(MatchResultEntity::getMatchId, request.getMatchId())
        );

        if (entity != null && Integer.valueOf(1).equals(entity.getStatus())) {
            throw new BusinessException(ResultCode.MATCH_RESULT_APPROVED);
        }

        boolean isNew = false;

        if (entity == null) {
            entity = new MatchResultEntity();
            entity.setMatchId(request.getMatchId());
            entity.setStatus(0);
            isNew = true;
        }

        fillResult(entity, request);

        if (isNew) {
            matchResultMapper.insert(entity);
        } else {
            matchResultMapper.updateById(entity);
        }

        match.setStatus(MATCH_STATUS_FINISHED);
        if (request.getMatchEndedAt() != null) {
            match.setActualEndTimeUtc(request.getMatchEndedAt());
        } else {
            match.setActualEndTimeUtc(LocalDateTime.now());
        }
        soccerMatchMapper.updateById(match);

        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "SAVE_MATCH_RESULT",
                "保存比赛结果并结束赛事成功，matchResultId=" + entity.getId()
                        + ", matchId=" + entity.getMatchId()
                        + ", isNew=" + isNew
        );
    }

    /**
     * 审核比赛结果。
     *
     * @param request 比赛结果审核参数
     */
    @Override
    public void approve(
            ApproveMatchResultRequest request) {
        requireRequest(request);
        SoccerMatchEntity match = requireMatchExists(request.getMatchId());
        requireMatchStatusAllowApprove(match);

        MatchResultEntity entity = matchResultMapper.selectOne(
                Wrappers.lambdaQuery(MatchResultEntity.class)
                        .eq(MatchResultEntity::getMatchId, request.getMatchId())
        );

        if (entity == null) {
            throw new BusinessException(ResultCode.MATCH_RESULT_NOT_FOUND);
        }

        if (Integer.valueOf(STATUS_APPROVED).equals(entity.getStatus())) {
            throw new BusinessException(ResultCode.MATCH_RESULT_APPROVED);
        }

        requireResultReadyToApprove(entity);

        entity.setStatus(STATUS_APPROVED);
        entity.setApprovedAt(LocalDateTime.now());

        matchResultMapper.updateById(entity);
        match.setStatus(MATCH_STATUS_FINISHED);
        soccerMatchMapper.updateById(match);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "APPROVE_MATCH_RESULT",
                "审核比赛结果成功，matchResultId=" + entity.getId()
                        + ", matchId=" + entity.getMatchId()
        );
    }

    /**
     * 使用非空请求字段填充比赛结果实体。
     *
     * @param entity  比赛结果实体
     * @param request 比赛结果保存参数
     */
    private void fillResult(
            MatchResultEntity entity,
            SaveMatchResultRequest request) {

        if (request.getRegularHomeScore() != null) {
            entity.setRegularHomeScore(request.getRegularHomeScore());
        }
        if (request.getRegularAwayScore() != null) {
            entity.setRegularAwayScore(request.getRegularAwayScore());
        }

        if (request.getExtraHomeScore() != null) {
            entity.setExtraHomeScore(request.getExtraHomeScore());
        }
        if (request.getExtraAwayScore() != null) {
            entity.setExtraAwayScore(request.getExtraAwayScore());
        }

        if (request.getPenaltyHomeScore() != null) {
            entity.setPenaltyHomeScore(request.getPenaltyHomeScore());
        }
        if (request.getPenaltyAwayScore() != null) {
            entity.setPenaltyAwayScore(request.getPenaltyAwayScore());
        }

        if (request.getMatchEndedAt() != null) {
            entity.setMatchEndedAt(request.getMatchEndedAt());
        }

        if (request.getHomePenaltyCount() != null) {
            entity.setHomePenaltyCount(request.getHomePenaltyCount());
        }
        if (request.getAwayPenaltyCount() != null) {
            entity.setAwayPenaltyCount(request.getAwayPenaltyCount());
        }

        if (request.getHomeCornerCount() != null) {
            entity.setHomeCornerCount(request.getHomeCornerCount());
        }
        if (request.getAwayCornerCount() != null) {
            entity.setAwayCornerCount(request.getAwayCornerCount());
        }

        if (request.getHomeThrowInCount() != null) {
            entity.setHomeThrowInCount(request.getHomeThrowInCount());
        }
        if (request.getAwayThrowInCount() != null) {
            entity.setAwayThrowInCount(request.getAwayThrowInCount());
        }

        if (request.getHomeFoulCount() != null) {
            entity.setHomeFoulCount(request.getHomeFoulCount());
        }
        if (request.getAwayFoulCount() != null) {
            entity.setAwayFoulCount(request.getAwayFoulCount());
        }

        if (request.getHomeFreeKickCount() != null) {
            entity.setHomeFreeKickCount(request.getHomeFreeKickCount());
        }
        if (request.getAwayFreeKickCount() != null) {
            entity.setAwayFreeKickCount(request.getAwayFreeKickCount());
        }

        if (request.getHomeRedCardCount() != null) {
            entity.setHomeRedCardCount(request.getHomeRedCardCount());
        }
        if (request.getAwayRedCardCount() != null) {
            entity.setAwayRedCardCount(request.getAwayRedCardCount());
        }

        if (request.getHomeYellowCardCount() != null) {
            entity.setHomeYellowCardCount(request.getHomeYellowCardCount());
        }
        if (request.getAwayYellowCardCount() != null) {
            entity.setAwayYellowCardCount(request.getAwayYellowCardCount());
        }
    }

    /**
     * 初始化分页参数。
     *
     * @param request 比赛结果分页查询条件
     */
    private void initPage(AdminMatchResultPageRequest request) {
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
     *
     * @param request 请求对象
     */
    private void requireRequest(Object request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验比赛存在。
     *
     * @param matchId 比赛 ID
     * @return 比赛实体
     */
    private SoccerMatchEntity requireMatchExists(Long matchId) {
        if (matchId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        SoccerMatchEntity match = soccerMatchMapper.selectById(matchId);
        if (match == null) {
            throw new BusinessException(ResultCode.SOCCER_NOT_EXISTS);
        }
        return match;
    }

    /**
     * 校验赛果达到审核条件。
     *
     * @param entity 比赛结果实体
     */
    private void requireResultReadyToApprove(MatchResultEntity entity) {
        if (entity.getRegularHomeScore() == null || entity.getRegularAwayScore() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    /**
     * 校验比赛状态允许审核赛果。
     *
     * @param match 比赛实体
     */
    private void requireMatchStatusAllowApprove(SoccerMatchEntity match) {
        if (MATCH_STATUS_CANCELLED.equals(match.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
    /**
     * 校验常规时间比分不能为空。
     *
     * @param request 保存赛果请求
     */
    private void requireResultScore(
            SaveMatchResultRequest request) {

        if (request.getRegularHomeScore() == null
                || request.getRegularAwayScore() == null) {

            throw new BusinessException(ResultCode.SCORE_CANT_NOT_NULL);
        }
    }
}
