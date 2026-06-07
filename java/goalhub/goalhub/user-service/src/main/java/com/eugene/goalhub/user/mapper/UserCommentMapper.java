package com.eugene.goalhub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.user.entity.UserCommentEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCommentMapper extends BaseMapper<UserCommentEntity> {
}