package com.eugene.goalhub.match.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.content.ContentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 内容 Mapper。
 *
 * <p>提供 content 表的基础 CRUD 能力。</p>
 */
@Mapper
public interface ContentMapper extends BaseMapper<ContentEntity> {
}
