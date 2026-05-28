package com.eugene.goalhub.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.admin.entity.AdminUserMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员与菜单权限关系表 Mapper。
 */
@Mapper
public interface AdminUserMenuMapper extends BaseMapper<AdminUserMenu> {
}
