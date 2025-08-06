package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema
public class VpnArticleRequestVO {
    @Schema(description = "资讯id,编辑时才需要,新增资讯不需要传这个参数")
    private Long id;
    @Schema(description = "资讯标题")
    private String articleTitle;
    @Schema(description = "资讯url")
    private String articleUrl;
    @Schema(description = "排序")
    private Integer sort;
    //扩展属性
    @Schema(description = "资讯正文")
    private String articleContent = "";
    @Schema(description = "资讯正文扩展(暂时不用)")
    private String articleContentextend = "";
    @Schema(description = "资讯发表时间")
    private LocalDateTime articleReleasetime = LocalDateTime.now();
    @Schema(description = "过期时间(暂时不用)")
    private LocalDateTime expireTime = LocalDateTime.now().plusDays(100);
    @Schema(description = "访问次数")
    private Integer visitedCount;
    @Schema(description = "文章是否可见")
    private Integer articleStatus = 0;
    @Schema(description = "备注")
    private String remark = "";
    @Schema(description = "图片url")
    private String ariticleImgurl = "";
}
