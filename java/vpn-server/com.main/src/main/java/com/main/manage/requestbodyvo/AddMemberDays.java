package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class AddMemberDays {
    @Schema(description = "会员id")
    private long memid;
    @Schema(description = "增加天数(负数为减少天数)")
    private Integer days;
}
