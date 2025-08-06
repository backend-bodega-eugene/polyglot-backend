package com.main.manage.requestbodyvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema
public class MemberRequestParamVO {
    @Schema(description = "页码(第几页)")
    private long pageIndex = 1;
    @Schema(description = "每页大小")
    private long pageSize = 10;
    @Schema(description = "会员账号")
    private String memName;
    @Schema(description = "注册时间起始 可以传空或者空字符 空或者空字符意味着无限往前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimeend;
    @Schema(description = "注册时间起始 可以传空或者空字符 空或者空字符意味着无限往后时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTimestart;
    @Schema(description = "是否会员 0非会员,1会员")
    private Integer isMember = -1;
    @Schema(description = "会员状态")
    private Integer memStatus = -1;
    @Schema(description = "邀请码")
    private String inventCode;
    @Schema(description = "上级邀请码")
    private String fatherIcode;
}
