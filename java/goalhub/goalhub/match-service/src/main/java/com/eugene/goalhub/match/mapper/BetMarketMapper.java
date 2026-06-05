package com.eugene.goalhub.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.BetMarketEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 投注玩法 Mapper。
 *
 * <p>负责投注玩法主表的通用 CRUD。</p>
 */
@Mapper
public interface BetMarketMapper extends BaseMapper<BetMarketEntity> {
}
