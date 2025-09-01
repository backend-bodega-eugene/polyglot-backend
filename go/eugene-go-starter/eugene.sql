/*
 Navicat Premium Data Transfer

 Source Server         : local
 Source Server Type    : MySQL
 Source Server Version : 90200 (9.2.0)
 Source Host           : localhost:3306
 Source Schema         : eugene

 Target Server Type    : MySQL
 Target Server Version : 90200 (9.2.0)
 File Encoding         : 65001

 Date: 01/09/2025 16:43:17
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for menus
-- ----------------------------
DROP TABLE IF EXISTS `menus`;
CREATE TABLE `menus`  (
  `menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID，主键，自增',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级菜单ID，0 表示顶级菜单',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径',
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `visible` tinyint NOT NULL DEFAULT 1 COMMENT '是否显示：1=显示，0=隐藏',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序值，数值越小越靠前',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '菜单状态：1=启用，0=禁用',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of menus
-- ----------------------------
INSERT INTO `menus` VALUES (1, 0, '用户管理', '/eugene/users.html', '', 'user', 1, 1, 1);
INSERT INTO `menus` VALUES (2, 0, '权限管理', '/eugene/roles.html', 'e', 'lock', 1, 2, 1);
INSERT INTO `menus` VALUES (3, 0, '菜单管理', '/eugene/menus.html', NULL, 'log', 0, 3, 1);
INSERT INTO `menus` VALUES (4, 0, '日志管理', '/eugene/logs.html', NULL, 'logs', 1, 0, 1);
INSERT INTO `menus` VALUES (5, 4, '错误日志', '/eugene/err_log.html', NULL, 'err_log', 1, 0, 1);
INSERT INTO `menus` VALUES (6, 4, '系统日志', '/eugene/sys_log.html', NULL, 'sys_log', 1, 0, 1);

-- ----------------------------
-- Table structure for user_menu
-- ----------------------------
DROP TABLE IF EXISTS `user_menu`;
CREATE TABLE `user_menu`  (
  `user_menu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户菜单关系ID，主键',
  `menu_id` bigint NOT NULL COMMENT '关联的菜单ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID',
  PRIMARY KEY (`user_menu_id`) USING BTREE,
  UNIQUE INDEX `uniq_user_menu`(`user_id` ASC, `menu_id` ASC) USING BTREE COMMENT '用户与菜单唯一约束，避免重复分配',
  INDEX `idx_user`(`user_id` ASC) USING BTREE COMMENT '用户索引',
  INDEX `idx_menu`(`menu_id` ASC) USING BTREE COMMENT '菜单索引'
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_menu
-- ----------------------------
INSERT INTO `user_menu` VALUES (3, 1, '2025-08-27 10:33:43', 2);
INSERT INTO `user_menu` VALUES (4, 2, '2025-08-27 10:33:43', 2);
INSERT INTO `user_menu` VALUES (5, 3, '2025-08-27 10:33:43', 2);
INSERT INTO `user_menu` VALUES (6, 5, '2025-08-27 10:33:43', 2);
INSERT INTO `user_menu` VALUES (7, 6, '2025-08-27 10:33:43', 2);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID，全局唯一，主键',
  `site_id` bigint UNSIGNED NOT NULL COMMENT '站点ID，用于区分不同站点/系统的数据隔离',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录名，在同一 site_id 下唯一',
  `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码的 bcrypt 哈希值',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '用户状态：1=正常，0=禁用',
  `last_login_at` datetime NULL DEFAULT NULL COMMENT '最近一次登录时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_site`(`site_id` ASC, `username` ASC) USING BTREE COMMENT '同一站点下用户名唯一',
  INDEX `idx_site`(`site_id` ASC) USING BTREE COMMENT '站点索引',
  INDEX `idx_last_login`(`last_login_at` ASC) USING BTREE COMMENT '最近登录时间索引'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理后台用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 10001, 'eugene', '$2a$10$vRDD/4zpG/DOpiA6sZRdWuwHfuAN6LjYcAr2v9FUcCSsNo0/XmPIe', 1, '2025-08-24 14:45:18', '2025-08-28 15:05:36');
INSERT INTO `users` VALUES (2, 10001, 'laozhang', '$2a$10$vRDD/4zpG/DOpiA6sZRdWuwHfuAN6LjYcAr2v9FUcCSsNo0/XmPIe', 1, NULL, '2025-08-28 15:05:40');

SET FOREIGN_KEY_CHECKS = 1;
