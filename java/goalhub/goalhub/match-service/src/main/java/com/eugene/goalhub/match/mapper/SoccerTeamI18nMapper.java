package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.SoccerTeamI18nEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 足球球队国际化 Mapper。
 *
 * <p>负责足球球队多语言配置表的通用 CRUD。</p>
 */
@Mapper
public interface SoccerTeamI18nMapper extends BaseMapper<SoccerTeamI18nEntity> {
}
