package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.BetMarketOptionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投注子玩法 Mapper。
 *
 * <p>负责投注玩法选项表的通用 CRUD。</p>
 */
@Mapper
public interface BetMarketOptionMapper extends BaseMapper<BetMarketOptionEntity> {
}
