package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.match.entity.SoccerTeamI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerTeamI18nMapper;
import com.eugene.goalhub.match.service.AdminTeamI18nService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.*;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

import java.util.List;

/**
 * 后台球队国际化配置管理服务实现。
 *
 * <p>负责球队多语言配置的查询、新增、更新、删除和响应转换。</p>
 */
@Service
public class AdminTeamI18nServiceImpl implements AdminTeamI18nService {

    /**
     * 业务日志模块名称。
     */
    private static final String MODULE_NAME = "后台球队国际化配置";

    /**
     * 球队国际化 Mapper。
     */
    private final SoccerTeamI18nMapper soccerTeamI18nMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建后台球队国际化配置管理服务实现。
     *
     * @param soccerTeamI18nMapper 球队国际化 Mapper
     */
    public AdminTeamI18nServiceImpl(SoccerTeamI18nMapper soccerTeamI18nMapper,
                                    MatchOperationLogger matchOperationLogger) {
        this.soccerTeamI18nMapper = soccerTeamI18nMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    @Override
    public List<TeamI18nResponse> listTeamI18n(TeamI18nListRequest request) {
        requireRequest(request);
        List<TeamI18nResponse> responses = soccerTeamI18nMapper.selectList(
                Wrappers.<SoccerTeamI18nEntity>lambdaQuery()
                        .eq(SoccerTeamI18nEntity::getTeamId, request.getTeamId())
                        .orderByAsc(SoccerTeamI18nEntity::getLangCode)
        ).stream().map(this::toResponse).toList();
        matchOperationLogger.sysLog(
                MODULE_NAME,
                "LIST_TEAM_I18N",
                "查询球队国际化配置列表，teamId=" + request.getTeamId()
                        + ", resultCount=" + responses.size()
        );
        return responses;
    }

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     */
    @Override
    public void addTeamI18n(AddTeamI18nRequest request) {
        requireRequest(request);
        SoccerTeamI18nEntity entity = new SoccerTeamI18nEntity();
        entity.setTeamId(request.getTeamId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        soccerTeamI18nMapper.insert(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "ADD_TEAM_I18N",
                "新增球队国际化配置成功，teamI18nId=" + entity.getId()
                        + ", teamId=" + entity.getTeamId()
                        + ", langCode=" + entity.getLangCode()
        );
    }

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     */
    @Override
    public void updateTeamI18n(UpdateTeamI18nRequest request) {
        requireRequest(request);
        requireId(request.getId());

        SoccerTeamI18nEntity entity = soccerTeamI18nMapper.selectById(request.getId());
        if (entity == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        entity.setTeamId(request.getTeamId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        updateOrThrow(entity);
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "UPDATE_TEAM_I18N",
                "更新球队国际化配置成功，teamI18nId=" + request.getId()
                        + ", teamId=" + request.getTeamId()
                        + ", langCode=" + request.getLangCode()
        );
    }

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     */
    @Override
    public void deleteTeamI18n(DeleteTeamI18nRequest request) {
        requireRequest(request);
        requireId(request.getId());
        deleteOrThrow(request.getId());
        matchOperationLogger.adminBizLog(
                MODULE_NAME,
                "DELETE_TEAM_I18N",
                "删除球队国际化配置成功，teamI18nId=" + request.getId()
        );
    }

    /**
     * 转换球队国际化实体为响应对象。
     *
     * @param entity 球队国际化实体
     * @return 球队国际化响应对象
     */
    private TeamI18nResponse toResponse(SoccerTeamI18nEntity entity) {
        TeamI18nResponse response = new TeamI18nResponse();
        response.setId(entity.getId());
        response.setTeamId(entity.getTeamId());
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

    private void updateOrThrow(SoccerTeamI18nEntity entity) {
        if (soccerTeamI18nMapper.updateById(entity) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }

    private void deleteOrThrow(Long id) {
        if (soccerTeamI18nMapper.deleteById(id) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }
    }
}
