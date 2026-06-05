package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.SoccerLeagueEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 足球联赛表 Mapper。
 *
 * <p>负责足球联赛基础表的通用 CRUD。</p>
 */
@Mapper
public interface SoccerLeagueMapper extends BaseMapper<SoccerLeagueEntity> {
}
