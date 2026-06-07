package com.eugene.goalhub.match.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.content.ContentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContentMapper extends BaseMapper<ContentEntity> {
}