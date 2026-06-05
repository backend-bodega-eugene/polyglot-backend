package com.eugene.goalhub.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 足球球队多语言实体，对应 soccer_team_i18n 表。
 *
 * <p>保存球队在不同语言下的展示名称和简称。</p>
 */
@TableName("soccer_team_i18n")
public class SoccerTeamI18nEntity {

    /**
     * 多语言记录 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 球队 ID。
     */
    private Long teamId;

    /**
     * 语言编码。
     */
    private String langCode;

    /**
     * 球队名称。
     */
    private String name;

    /**
     * 球队简称。
     */
    private String shortName;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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


}
