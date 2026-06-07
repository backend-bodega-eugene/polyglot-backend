package com.eugene.goalhub.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.user.entity.UserCommentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户留言 Mapper。
 *
 * <p>负责用户客服留言表的基础 CRUD 操作。</p>
 */
@Mapper
public interface UserCommentMapper extends BaseMapper<UserCommentEntity> {
}
