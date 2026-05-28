package com.eugene.goalhub.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 后台菜单实体，对应 admin_menu 表。
 */
@TableName("admin_menu")
public class AdminMenu {

    /**
     * 菜单主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父菜单 ID，0 表示顶级菜单。
     */
    private Long parentId;

    /**
     * 菜单名称。
     */
    private String name;

    /**
     * 菜单类型：1 目录，2 菜单，3 按钮。
     */
    private Integer type;

    /**
     * 前端路由路径或按钮权限标识。
     */
    private String path;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 排序值，数值越小越靠前。
     */
    private Integer sortOrder;

    /**
     * 状态：1 启用，0 禁用。
     */
    private Integer status;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除标记：1 已删除，0 未删除。
     */
    @TableLogic
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}
