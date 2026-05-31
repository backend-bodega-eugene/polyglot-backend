package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eugene.goalhub.match.entity.SoccerMatchI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerMatchI18nMapper;
import com.eugene.goalhub.match.service.AdminMatchI18nService;
import dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 后台比赛国际化配置管理服务实现。
 */
@Service
public class AdminMatchI18nServiceImpl implements AdminMatchI18nService {

    private final SoccerMatchI18nMapper soccerMatchI18nMapper;

    public AdminMatchI18nServiceImpl(SoccerMatchI18nMapper soccerMatchI18nMapper) {
        this.soccerMatchI18nMapper = soccerMatchI18nMapper;
    }

    /**
     * 查询比赛国际化配置列表。
     *
     * @param request 比赛国际化查询条件
     * @return 比赛国际化配置列表
     */
    @Override
    public List<MatchI18nResponse> listMatchI18n(MatchI18nListRequest request) {
        return soccerMatchI18nMapper.selectList(
                Wrappers.<SoccerMatchI18nEntity>lambdaQuery()
                        .eq(SoccerMatchI18nEntity::getMatchId, request.getMatchId())
                        .orderByAsc(SoccerMatchI18nEntity::getLangCode)
        ).stream().map(this::toResponse).toList();
    }

    /**
     * 新增比赛国际化配置。
     *
     * @param request 比赛国际化新增参数
     */
    @Override
    public void addMatchI18n(AddMatchI18nRequest request) {
        SoccerMatchI18nEntity entity = new SoccerMatchI18nEntity();
        entity.setMatchId(request.getMatchId());
        entity.setLangCode(request.getLangCode());
        entity.setMatchName(request.getMatchName());
        entity.setStageName(request.getStageName());
        entity.setCity(request.getCity());
        entity.setVenue(request.getVenue());

        soccerMatchI18nMapper.insert(entity);
    }

    /**
     * 更新比赛国际化配置。
     *
     * @param request 比赛国际化更新参数
     */
    @Override
    public void updateMatchI18n(UpdateMatchI18nRequest request) {
        SoccerMatchI18nEntity entity = new SoccerMatchI18nEntity();
        entity.setId(request.getId());
        entity.setMatchId(request.getMatchId());
        entity.setLangCode(request.getLangCode());
        entity.setMatchName(request.getMatchName());
        entity.setStageName(request.getStageName());
        entity.setCity(request.getCity());
        entity.setVenue(request.getVenue());

        soccerMatchI18nMapper.updateById(entity);
    }

    /**
     * 删除比赛国际化配置。
     *
     * @param request 比赛国际化删除参数
     */
    @Override
    public void deleteMatchI18n(DeleteMatchI18nRequest request) {
        soccerMatchI18nMapper.deleteById(request.getId());
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
}
