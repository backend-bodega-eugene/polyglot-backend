package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.BetMarketEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投注玩法 Mapper。
 */
@Mapper
public interface BetMarketMapper extends BaseMapper<BetMarketEntity> {
}