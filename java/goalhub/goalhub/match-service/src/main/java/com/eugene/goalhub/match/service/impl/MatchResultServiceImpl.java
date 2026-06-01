package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eugene.goalhub.match.entity.MatchResultEntity;
import com.eugene.goalhub.match.mapper.MatchResultMapper;
import com.eugene.goalhub.match.service.MatchResultService;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.time.LocalDateTime;

/**
 * 比赛结果管理服务实现。
 */
@Service
public class MatchResultServiceImpl implements MatchResultService {

    /**
     * 比赛结果 Mapper。
     */
    private final MatchResultMapper matchResultMapper;

    /**
     * 创建比赛结果管理服务实现。
     *
     * @param matchResultMapper 比赛结果 Mapper
     */
    public MatchResultServiceImpl(
            MatchResultMapper matchResultMapper) {
        this.matchResultMapper = matchResultMapper;
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

        Page<AdminMatchResultResponse> page = new Page<>(
                request.getPageIndex(),
                request.getPageSize()
        );

        Page<AdminMatchResultResponse> result =
                matchResultMapper.adminPage(page, request);

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
    public void save(
            SaveMatchResultRequest request) {

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
    }

    /**
     * 审核比赛结果。
     *
     * @param request 比赛结果审核参数
     */
    @Override
    public void approve(
            ApproveMatchResultRequest request) {

        MatchResultEntity entity = matchResultMapper.selectOne(
                Wrappers.lambdaQuery(MatchResultEntity.class)
                        .eq(MatchResultEntity::getMatchId, request.getMatchId())
        );

        if (entity == null) {
            throw new BusinessException(ResultCode.MATCH_RESULT_NOT_FOUND);
        }

        entity.setStatus(1);
        entity.setApprovedAt(LocalDateTime.now());

        matchResultMapper.updateById(entity);
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
}
