package com.app.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vpn_article")
public class VpnArticle extends AbstractEntity {
    @Schema(description = "资讯标题")
    private String articleTitle;
    @Schema(description = "资讯正文")
    private String articleContent;
    @Schema(description = "资讯正文扩展(暂时不用)")
    private String articleContentextend;
    @Schema(description = "资讯发表时间")
    private LocalDateTime articleReleasetime;
    @Schema(description = "资讯url")
    private String articleUrl;
    @Schema(description = "过期时间(暂时不用)")
    private LocalDateTime expireTime;
    @Schema(description = "访问次数")
    private Integer visitedCount;
    @Schema(description = "文章是否可见")
    private Integer articleStatus;
    @Schema(description = "排序")
    private Integer sort;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "备注")
    private String ariticleImgurl;
}
