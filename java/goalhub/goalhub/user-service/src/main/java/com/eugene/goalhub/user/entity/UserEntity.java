package com.eugene.goalhub.user.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;

/**
 * 应用用户实体，对应 users 表。
 */
@TableName("users")
public class UserEntity {

    /**
     * 用户 ID。
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 手机号。
     */
    private String phone;

    /**
     * BCrypt 加密后的登录密码。
     */
    private String passwordHash;

    /**
     * 昵称。
     */
    private String nickname;

    /**
     * 头像地址。
     */
    private String avatarUrl;

    /**
     * 账号状态：1 启用，0 禁用。
     */
    private Integer status;

    /**
     * 最近登录时间。
     */
    private LocalDateTime lastLoginAt;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
    /**
     * 邀请码。
     */
    private String inviteCode;

    /**
     * 邀请人用户ID。
     */
    private Long inviterUserId;

    /**
     * 注册IP。
     */
    private String registerIp;

    /**
     * 最后登录IP。
     */
    private String lastLoginIp;
    public String getInviteCode() {
        return inviteCode;
    }

    public UserEntity setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
        return this;
    }

    public Long getInviterUserId() {
        return inviterUserId;
    }

    public UserEntity setInviterUserId(Long inviterUserId) {
        this.inviterUserId = inviterUserId;
        return this;
    }

    public String getRegisterIp() {
        return registerIp;
    }

    public UserEntity setRegisterIp(String registerIp) {
        this.registerIp = registerIp;
        return this;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public UserEntity setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
        return this;
    }

    public Long getId() {
        return id;
    }

    public UserEntity setId(Long id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserEntity setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserEntity setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserEntity setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserEntity setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public UserEntity setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public UserEntity setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public UserEntity setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public UserEntity setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
        return this;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UserEntity setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UserEntity setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
