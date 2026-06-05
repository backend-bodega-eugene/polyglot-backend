package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.match.entity.SoccerMatchI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerMatchI18nMapper;
import com.eugene.goalhub.match.service.AdminMatchI18nService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 后台比赛国际化配置管理服务实现。
 *
 * <p>负责比赛多语言配置的查询、新增、更新、删除和响应转换。</p>
 */
@Service
public class AdminMatchI18nServiceImpl implements AdminMatchI18nService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "后台比赛国际化配置";

    /**
     * 比赛国际化 Mapper。
     */
    private final SoccerMatchI18nMapper soccerMatchI18nMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建后台比赛国际化配置管理服务实现。
     *
     * @param soccerMatchI18nMapper 比赛国际化 Mapper
     */
    public AdminMatchI18nServiceImpl(SoccerMatchI18nMapper soccerMatchI18nMapper,
                                     MatchOperationLogger matchOperationLogger) {
        this.soccerMatchI18nMapper = soccerMatchI18nMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    @Override
    public List<MatchI18nResponse> listMatchI18n(MatchI18nListRequest request) {
        requireRequest(request);
        List<MatchI18nResponse> responses = soccerMatchI18nMapper.selectList(
                Wrappers.<SoccerMatchI18nEntity>lambdaQuery()
                        .eq(SoccerMatchI18nEntity::getMatchId, request.getMatchId())
                        .orderByAsc(SoccerMatchI18nEntity::getLangCode)
        ).stream().map(this::toResponse).toList();
        matchOperationLogger.sysLog(
                MODULE_NAME,
                "LIST_MATCH_I18N",
                "查询比赛国际化配置列表，matchId=" + request.getMatchId()
                        + ", resultCount=" + responses.size()
        );
        return responses;
    }

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     */
    @Override
    public void addMatchI18n(AddMatchI18nRequest request) {
        requireRequest(request);
        SoccerMatchI18nEntity entity = new SoccerMatchI18nEntity();
        entity.setMatchId(request.getMatchId());
        entity.setLangCode(request.getLangCode());
        entity.setMatchName(request.getMatchName());
        entity.setStageName(request.getStageName());
        entity.setCity(request.getCity());
        entity.setVenue(request.getVenue());

        soccerMatchI18nMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_MATCH_I18N",
                "新增比赛国际化配置成功，matchI18nId=" + entity.getId()
                        + ", matchId=" + entity.getMatchId()
                        + ", langCode=" + entity.getLangCode()
        );
    }

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     */
    @Override
    public void updateMatchI18n(UpdateMatchI18nRequest request) {
        requireRequest(request);
        requireId(request.getId());

        SoccerMatchI18nEntity entity = soccerMatchI18nMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        entity.setMatchId(request.getMatchId());
        entity.setLangCode(request.getLangCode());
        entity.setMatchName(request.getMatchName());
        entity.setStageName(request.getStageName());
        entity.setCity(request.getCity());
        entity.setVenue(request.getVenue());

        updateOrThrow(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_MATCH_I18N",
                "更新比赛国际化配置成功，matchI18nId=" + request.getId()
                        + ", matchId=" + request.getMatchId()
                        + ", langCode=" + request.getLangCode()
        );
    }

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     */
    @Override
    public void deleteMatchI18n(DeleteMatchI18nRequest request) {
        requireRequest(request);
        requireId(request.getId());
        deleteOrThrow(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_MATCH_I18N",
                "删除比赛国际化配置成功，matchI18nId=" + request.getId()
        );
    }

    /**
     * 转换比赛国际化实体为响应对象。
     *
     * @param entity 比赛国际化实体
     * @return 比赛国际化响应对象
     */
    private MatchI18nResponse toResponse(SoccerMatchI18nEntity entity) {
        MatchI18nResponse response = new MatchI18nResponse();
        response.setId(entity.getId());
        response.setMatchId(entity.getMatchId());
        response.setLangCode(entity.getLangCode());
        response.setMatchName(entity.getMatchName());
        response.setStageName(entity.getStageName());
        response.setCity(entity.getCity());
        response.setVenue(entity.getVenue());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private void requireRequest(Object request) {
        if (request == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void requireId(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void updateOrThrow(SoccerMatchI18nEntity entity) {
        if (soccerMatchI18nMapper.updateById(entity) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void deleteOrThrow(Long id) {
        if (soccerMatchI18nMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
