package com.eugene.goalhub.admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.admin.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台管理员账号表 Mapper。
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
