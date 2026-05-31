package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.match.entity.SoccerLeagueI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerLeagueI18nMapper;
import com.eugene.goalhub.match.service.AdminLeagueI18nService;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台联赛国际化配置管理服务实现。
 */
@Service
public class AdminLeagueI18nServiceImpl implements AdminLeagueI18nService {

    private final SoccerLeagueI18nMapper soccerLeagueI18nMapper;

    public AdminLeagueI18nServiceImpl(SoccerLeagueI18nMapper soccerLeagueI18nMapper) {
        this.soccerLeagueI18nMapper = soccerLeagueI18nMapper;
    }

    /**
     * 查询联赛国际化配置列表。
     *
     * @param request 联赛国际化查询条件
     * @return 联赛国际化配置列表
     */
    @Override
    public List<LeagueI18nResponse> listLeagueI18n(LeagueI18nListRequest request) {
        return soccerLeagueI18nMapper.selectList(
                Wrappers.<SoccerLeagueI18nEntity>lambdaQuery()
                        .eq(SoccerLeagueI18nEntity::getLeagueId, request.getLeagueId())
                        .orderByAsc(SoccerLeagueI18nEntity::getLangCode)
        ).stream().map(this::toResponse).toList();
    }

    /**
     * 新增联赛国际化配置。
     *
     * @param request 联赛国际化新增参数
     */
    @Override
    public void addLeagueI18n(AddLeagueI18nRequest request) {
        SoccerLeagueI18nEntity entity = new SoccerLeagueI18nEntity();
        entity.setLeagueId(request.getLeagueId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        soccerLeagueI18nMapper.insert(entity);
    }

    /**
     * 更新联赛国际化配置。
     *
     * @param request 联赛国际化更新参数
     */
    @Override
    public void updateLeagueI18n(UpdateLeagueI18nRequest request) {
        SoccerLeagueI18nEntity entity = new SoccerLeagueI18nEntity();
        entity.setId(request.getId());
        entity.setLeagueId(request.getLeagueId());
        entity.setLangCode(request.getLangCode());
        entity.setName(request.getName());
        entity.setShortName(request.getShortName());

        soccerLeagueI18nMapper.updateById(entity);
    }

    /**
     * 删除联赛国际化配置。
     *
     * @param request 联赛国际化删除参数
     */
    @Override
    public void deleteLeagueI18n(DeleteLeagueI18nRequest request) {
        soccerLeagueI18nMapper.deleteById(request.getId());
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
}
