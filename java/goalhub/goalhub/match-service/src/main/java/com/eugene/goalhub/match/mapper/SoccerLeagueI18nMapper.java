package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.SoccerLeagueI18nEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 足球联赛多语言表 Mapper。
 *
 * <p>负责足球联赛多语言配置表的通用 CRUD。</p>
 */
@Mapper
public interface SoccerLeagueI18nMapper extends BaseMapper<SoccerLeagueI18nEntity> {
}
