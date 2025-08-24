-- 创建数据库
CREATE DATABASE IF NOT EXISTS `eugene` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_general_ci
  COMMENT='eugene 项目数据库';

-- 使用数据库
USE `eugene`;

-- 菜单表
CREATE TABLE `menus` (
  `menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '菜单ID，主键，自增',
  `parent_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '父级菜单ID，0 表示顶级菜单',
  `name` varchar(64) NOT NULL COMMENT '菜单名称',
  `path` varchar(128) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(128) DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(64) DEFAULT NULL COMMENT '菜单图标',
  `visible` tinyint(4) NOT NULL DEFAULT '1' COMMENT '是否显示：1=显示，0=隐藏',
  `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序值，数值越小越靠前',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '菜单状态：1=启用，0=禁用',
  PRIMARY KEY (`menu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 用户菜单关联表
CREATE TABLE `user_menu` (
  `user_menu_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户菜单关系ID，主键',
  `menu_id` bigint(20) NOT NULL COMMENT '关联的菜单ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` bigint(20) NOT NULL COMMENT '关联的用户ID',
  PRIMARY KEY (`user_menu_id`),
  UNIQUE KEY `uniq_user_menu` (`user_id`,`menu_id`) COMMENT '用户和菜单的唯一约束，避免重复分配',
  KEY `idx_user` (`user_id`) COMMENT '用户索引',
  KEY `idx_menu` (`menu_id`) COMMENT '菜单索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-菜单关联表';

-- 用户表
CREATE TABLE `users` (
  `user_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID，全局唯一，主键',
  `site_id` bigint(20) unsigned NOT NULL COMMENT '站点ID，用于区分不同站点/系统的数据隔离',
  `username` varchar(64) NOT NULL COMMENT '登录名，在同一 site_id 下唯一',
  `password_hash` varchar(100) NOT NULL COMMENT '用户密码的 bcrypt 哈希值',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '用户状态：1=正常，0=禁用',
  `last_login_at` datetime DEFAULT NULL COMMENT '最近一次登录时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_site` (`site_id`,`username`) COMMENT '同一站点下用户名唯一',
  KEY `idx_site` (`site_id`) COMMENT '站点索引',
  KEY `idx_last_login` (`last_login_at`) COMMENT '最近登录时间索引'
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='管理后台用户表';
