package com.eugene.goalhub.match.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eugene.goalhub.match.entity.SoccerLeagueEntity;
import com.eugene.goalhub.match.entity.SoccerLeagueI18nEntity;
import com.eugene.goalhub.match.mapper.SoccerLeagueI18nMapper;
import com.eugene.goalhub.match.mapper.SoccerLeagueMapper;
import com.eugene.goalhub.match.service.SoccerLeagueService;
import dto.SoccerLeagueResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class SoccerLeagueServiceImpl
        extends ServiceImpl<SoccerLeagueMapper, SoccerLeagueEntity>
        implements SoccerLeagueService {

    private final SoccerLeagueI18nMapper soccerLeagueI18nMapper;

    public SoccerLeagueServiceImpl(SoccerLeagueI18nMapper soccerLeagueI18nMapper) {
        this.soccerLeagueI18nMapper = soccerLeagueI18nMapper;
    }

    @Override
    public List<SoccerLeagueResponse> listLeagues(String keyword, String langCode) {

        String finalLangCode = StringUtils.hasText(langCode) ? langCode : "en-US";

        List<SoccerLeagueEntity> leagues = lambdaQuery()
                .eq(SoccerLeagueEntity::getStatus, 1)
                .orderByAsc(SoccerLeagueEntity::getId)
                .list();

        return leagues.stream()
                .map(league -> buildResponse(league, keyword, finalLangCode))
                .filter(response -> response != null)
                .toList();
    }

    private SoccerLeagueResponse buildResponse(SoccerLeagueEntity league,
                                               String keyword,
                                               String langCode) {

        SoccerLeagueI18nEntity i18n = findI18n(league.getId(), langCode);

        if (i18n == null) {
            i18n = findI18n(league.getId(), "en-US");
        }

        if (i18n == null) {
            return null;
        }

        if (StringUtils.hasText(keyword)) {
            boolean matchedName = i18n.getName() != null && i18n.getName().contains(keyword);
            boolean matchedShortName = i18n.getShortName() != null && i18n.getShortName().contains(keyword);
            boolean matchedCode = league.getCode() != null && league.getCode().contains(keyword);

            if (!matchedName && !matchedShortName && !matchedCode) {
                return null;
            }
        }

        SoccerLeagueResponse response = new SoccerLeagueResponse();
        response.setId(league.getId());
        response.setCode(league.getCode());
        response.setName(i18n.getName());
        response.setShortName(i18n.getShortName());
        response.setHostCountry(league.getHostCountry());
        response.setLogoUrl(league.getLogoUrl());

        return response;
    }

    private SoccerLeagueI18nEntity findI18n(Long leagueId, String langCode) {

        return soccerLeagueI18nMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers
                        .<SoccerLeagueI18nEntity>lambdaQuery()
                        .eq(SoccerLeagueI18nEntity::getLeagueId, leagueId)
                        .eq(SoccerLeagueI18nEntity::getLangCode, langCode)
        );
    }
}