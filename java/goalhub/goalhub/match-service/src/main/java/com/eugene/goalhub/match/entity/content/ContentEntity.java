package com.eugene.goalhub.match.entity.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;


import java.time.LocalDateTime;

/**
 * 内容实体，对应 content 表。
 */
@TableName("content")
public class ContentEntity {

    /**
     * 内容 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 内容类型。
     */
    private String type;

    /**
     * 内容标题。
     */
    private String title;

    /**
     * 内容摘要。
     */
    private String summary;

    /**
     * 封面图片地址。
     */
    private String coverUrl;

    /**
     * HTML 格式正文。
     */
    private String contentHtml;

    /**
     * 内容状态。
     */
    private String status;

    /**
     * 排序值。
     */
    private Integer sort;

    /**
     * 发布时间。
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;

    /**
     * 获取内容 ID。
     *
     * @return 内容 ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置内容 ID。
     *
     * @param id 内容 ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取内容类型。
     *
     * @return 内容类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置内容类型。
     *
     * @param type 内容类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取内容标题。
     *
     * @return 内容标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置内容标题。
     *
     * @param title 内容标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取内容摘要。
     *
     * @return 内容摘要
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 设置内容摘要。
     *
     * @param summary 内容摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 获取封面图片地址。
     *
     * @return 封面图片地址
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * 设置封面图片地址。
     *
     * @param coverUrl 封面图片地址
     */
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * 获取 HTML 格式正文。
     *
     * @return HTML 格式正文
     */
    public String getContentHtml() {
        return contentHtml;
    }

    /**
     * 设置 HTML 格式正文。
     *
     * @param contentHtml HTML 格式正文
     */
    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    /**
     * 获取内容状态。
     *
     * @return 内容状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置内容状态。
     *
     * @param status 内容状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取排序值。
     *
     * @return 排序值
     */
    public Integer getSort() {
        return sort;
    }

    /**
     * 设置排序值。
     *
     * @param sort 排序值
     */
    public void setSort(Integer sort) {
        this.sort = sort;
    }

    /**
     * 获取发布时间。
     *
     * @return 发布时间
     */
    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    /**
     * 设置发布时间。
     *
     * @param publishTime 发布时间
     */
    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
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
