package com.eugene.goalhub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper。
 *
 * <p>负责 users 表的基础 CRUD。</p>
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
}
