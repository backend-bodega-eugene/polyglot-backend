package com.eugene.goalhub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 管理员与菜单权限关系实体，对应 admin_user_menu 表。
 */
@TableName("admin_user_menu")
public class AdminUserMenu {

    /**
     * 关系主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 管理员 ID。
     */
    private Long adminUserId;

    /**
     * 菜单 ID。
     */
    private Long menuId;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 获取关系主键 ID。
     *
     * @return 关系主键 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置关系主键 ID。
     *
     * @param id 关系主键 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取管理员 ID。
     *
     * @return 管理员 ID
     */
    public Long getAdminUserId() {
        return adminUserId;
    }

    /**
     * 设置管理员 ID。
     *
     * @param adminUserId 管理员 ID
     */
    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    /**
     * 获取菜单 ID。
     *
     * @return 菜单 ID
     */
    public Long getMenuId() {
        return menuId;
    }

    /**
     * 设置菜单 ID。
     *
     * @param menuId 菜单 ID
     */
    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    /**
     * 获取创建时间。
     *
     * @return 创建时间
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 设置创建时间。
     *
     * @param createdAt 创建时间
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取更新时间。
     *
     * @return 更新时间
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 设置更新时间。
     *
     * @param updatedAt 更新时间
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
