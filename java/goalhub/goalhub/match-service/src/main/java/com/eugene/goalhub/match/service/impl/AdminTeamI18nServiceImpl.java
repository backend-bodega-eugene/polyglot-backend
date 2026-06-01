package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.match.entity.SoccerTeamI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerTeamI18nMapper;
import com.eugene.goalhub.match.service.AdminTeamI18nService;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台球队国际化配置管理服务实现。
 */
@Service
public class AdminTeamI18nServiceImpl implements AdminTeamI18nService {

    /**
     * 球队国际化 Mapper。
     */
    private final SoccerTeamI18nMapper soccerTeamI18nMapper;

    /**
     * 创建后台球队国际化配置管理服务实现。
     *
     * @param soccerTeamI18nMapper 球队国际化 Mapper
     */
    public AdminTeamI18nServiceImpl(SoccerTeamI18nMapper soccerTeamI18nMapper) {
        this.soccerTeamI18nMapper = soccerTeamI18nMapper;
    }

    /**
     * 查询球队国际化配置列表。
     *
     * @param request 球队国际化查询条件
     * @return 球队国际化配置列表
     */
    @Override
    public List<TeamI18nResponse> listTeamI18n(TeamI18nListRequest request) {
        return soccerTeamI18nMapper.selectList(
                Wrappers.<SoccerTeamI18nEntity>lambdaQuery()
                        .eq(SoccerTeamI18nEntity::getTeamId, request.getTeamId())
                        .orderByAsc(SoccerTeamI18nEntity::getLangCode)
        ).stream().map(this::toResponse).toList();
    }

    /**
     * 新增球队国际化配置。
     *
     * @param request 球队国际化新增参数
     */
    @Override
    public void addTeamI18n(AddTeamI18nRequest request) {
        SoccerTeamI18nEntity entity = new SoccerTeamI18nEntity();
        entity.setTeamId(request.getTeamId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        soccerTeamI18nMapper.insert(entity);
    }

    /**
     * 更新球队国际化配置。
     *
     * @param request 球队国际化更新参数
     */
    @Override
    public void updateTeamI18n(UpdateTeamI18nRequest request) {
        SoccerTeamI18nEntity entity = new SoccerTeamI18nEntity();
        entity.setId(request.getId());
        entity.setTeamId(request.getTeamId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        soccerTeamI18nMapper.updateById(entity);
    }

    /**
     * 删除球队国际化配置。
     *
     * @param request 球队国际化删除参数
     */
    @Override
    public void deleteTeamI18n(DeleteTeamI18nRequest request) {
        soccerTeamI18nMapper.deleteById(request.getId());
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
}
