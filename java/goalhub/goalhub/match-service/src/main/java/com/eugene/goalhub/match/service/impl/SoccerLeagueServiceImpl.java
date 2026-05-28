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

/**
 * 足球联赛查询服务实现。
 */
@Service
public class SoccerLeagueServiceImpl
        extends ServiceImpl<SoccerLeagueMapper, SoccerLeagueEntity>
        implements SoccerLeagueService {

    /**
     * 联赛多语言 Mapper。
     */
    private final SoccerLeagueI18nMapper soccerLeagueI18nMapper;

    public SoccerLeagueServiceImpl(SoccerLeagueI18nMapper soccerLeagueI18nMapper) {
        this.soccerLeagueI18nMapper = soccerLeagueI18nMapper;
    }

    /**
     * 查询启用联赛，并按指定语言返回名称。
     *
     * @param keyword  联赛名称、简称或编码关键字
     * @param langCode 语言编码
     * @return 联赛列表
     */
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

    /**
     * 构建联赛响应对象。
     * <p>
     * 如果指定语言不存在，会回退到 en-US；没有可用翻译时返回 null。
     *
     * @param league   联赛实体
     * @param keyword  搜索关键字
     * @param langCode 语言编码
     * @return 联赛响应对象
     */
    private SoccerLeagueResponse buildResponse(SoccerLeagueEntity league,
                                               String keyword,
                                               String langCode) {

        SoccerLeagueI18nEntity i18n = findI18n(league.getId(), langCode);

        if (i18n == null) {
            // 指定语言没有翻译时，默认回退到英文。
            i18n = findI18n(league.getId(), "en-US");
        }

        if (i18n == null) {
            return null;
        }

        if (StringUtils.hasText(keyword)) {
            // 关键字支持匹配联赛名称、简称和联赛编码。
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

    /**
     * 查询指定联赛和语言的多语言记录。
     *
     * @param leagueId 联赛 ID
     * @param langCode 语言编码
     * @return 联赛多语言记录
     */
    private SoccerLeagueI18nEntity findI18n(Long leagueId, String langCode) {

        return soccerLeagueI18nMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers
                        .<SoccerLeagueI18nEntity>lambdaQuery()
                        .eq(SoccerLeagueI18nEntity::getLeagueId, leagueId)
                        .eq(SoccerLeagueI18nEntity::getLangCode, langCode)
        );
    }
}
