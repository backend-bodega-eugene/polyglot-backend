package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.match.entity.SoccerLeagueI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerLeagueI18nMapper;
import com.eugene.goalhub.match.service.AdminLeagueI18nService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 后台联赛国际化配置管理服务实现。
 *
 * <p>负责联赛多语言配置的查询、新增、更新、删除和响应转换。</p>
 */
@Service
public class AdminLeagueI18nServiceImpl implements AdminLeagueI18nService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "后台联赛国际化配置";

    /**
     * 联赛国际化 Mapper。
     */
    private final SoccerLeagueI18nMapper soccerLeagueI18nMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建后台联赛国际化配置管理服务实现。
     *
     * @param soccerLeagueI18nMapper 联赛国际化 Mapper
     */
    public AdminLeagueI18nServiceImpl(SoccerLeagueI18nMapper soccerLeagueI18nMapper,
                                      MatchOperationLogger matchOperationLogger) {
        this.soccerLeagueI18nMapper = soccerLeagueI18nMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    @Override
    public List<LeagueI18nResponse> listLeagueI18n(LeagueI18nListRequest request) {
        requireRequest(request);
        List<LeagueI18nResponse> responses = soccerLeagueI18nMapper.selectList(
                Wrappers.<SoccerLeagueI18nEntity>lambdaQuery()
                        .eq(SoccerLeagueI18nEntity::getLeagueId, request.getLeagueId())
                        .orderByAsc(SoccerLeagueI18nEntity::getLangCode)
        ).stream().map(this::toResponse).toList();
        matchOperationLogger.sysLog(
                MODULE_NAME,
                "LIST_LEAGUE_I18N",
                "查询联赛国际化配置列表，leagueId=" + request.getLeagueId()
                        + ", resultCount=" + responses.size()
        );
        return responses;
    }

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     */
    @Override
    public void addLeagueI18n(AddLeagueI18nRequest request) {
        requireRequest(request);
        SoccerLeagueI18nEntity entity = new SoccerLeagueI18nEntity();
        entity.setLeagueId(request.getLeagueId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        soccerLeagueI18nMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_LEAGUE_I18N",
                "新增联赛国际化配置成功，leagueI18nId=" + entity.getId()
                        + ", leagueId=" + entity.getLeagueId()
                        + ", langCode=" + entity.getLangCode()
        );
    }

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     */
    @Override
    public void updateLeagueI18n(UpdateLeagueI18nRequest request) {
        requireRequest(request);
        requireId(request.getId());

        SoccerLeagueI18nEntity entity = soccerLeagueI18nMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        entity.setLeagueId(request.getLeagueId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        updateOrThrow(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_LEAGUE_I18N",
                "更新联赛国际化配置成功，leagueI18nId=" + request.getId()
                        + ", leagueId=" + request.getLeagueId()
                        + ", langCode=" + request.getLangCode()
        );
    }

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     */
    @Override
    public void deleteLeagueI18n(DeleteLeagueI18nRequest request) {
        requireRequest(request);
        requireId(request.getId());
        deleteOrThrow(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_LEAGUE_I18N",
                "删除联赛国际化配置成功，leagueI18nId=" + request.getId()
        );
    }

    /**
     * 转换联赛国际化实体为响应对象。
     *
     * @param entity 联赛国际化实体
     * @return 联赛国际化响应对象
     */
    private LeagueI18nResponse toResponse(SoccerLeagueI18nEntity entity) {
        LeagueI18nResponse response = new LeagueI18nResponse();
        response.setId(entity.getId());
        response.setLeagueId(entity.getLeagueId());
        response.setLangCode(entity.getLangCode());
        response.setName(entity.getName());
        response.setShortName(entity.getShortName());
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

    private void updateOrThrow(SoccerLeagueI18nEntity entity) {
        if (soccerLeagueI18nMapper.updateById(entity) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void deleteOrThrow(Long id) {
        if (soccerLeagueI18nMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
