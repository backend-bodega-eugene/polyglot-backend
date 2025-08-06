package com.main.manage.requestbodyvo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema
public class EditMemberVO {
    @Schema(description = "会员id")
    private long memid;
    @Schema(description = "会员状态")
    private Integer status;
    @Schema(description = "会员上次过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime olddatetime;
    @Schema(description = "会员最新过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime newdateTime;
    @Schema(description = "用户的会员状态")
    private int isMember;

}
