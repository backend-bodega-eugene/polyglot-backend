package com.eugene.goalhub.match.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.match.entity.UserMatchFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户赛事关注表 Mapper。
 */
@Mapper
public interface UserMatchFollowMapper extends BaseMapper<UserMatchFollow> {
}
