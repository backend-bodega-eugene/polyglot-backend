package com.eugene.goalhub.match.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 足球联赛多语言实体，对应 soccer_league_i18n 表。
 *
 * <p>保存联赛在不同语言下的展示名称和简称。</p>
 */
@TableName("soccer_league_i18n")
public class SoccerLeagueI18nEntity {

    /**
     * 多语言记录 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 联赛 ID。
     */
    private Long leagueId;

    /**
     * 语言编码。
     */
    private String langCode;

    /**
     * 联赛名称。
     */
    private String name;

    /**
     * 联赛简称。
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

    public Long getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Long leagueId) {
        this.leagueId = leagueId;
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
