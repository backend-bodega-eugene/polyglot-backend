package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class MemberStatusVO {
    @Schema(description = "会员id(游客)")
    private long memId;
    @Schema(description = "会员状态")
    private Integer memStatus;
}
