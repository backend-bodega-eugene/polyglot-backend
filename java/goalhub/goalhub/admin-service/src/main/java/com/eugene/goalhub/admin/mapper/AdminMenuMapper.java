package com.eugene.goalhub.admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eugene.goalhub.admin.entity.AdminMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 后台菜单表 Mapper。
 */
@Mapper
public interface AdminMenuMapper extends BaseMapper<AdminMenu> {
}
