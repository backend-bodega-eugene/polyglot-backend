package com.app.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("visitor_info")
public class VisitorInfo {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "游客id")
    protected Long id;
    @Schema(description = "游客名称")
    private String visitorName;
    @Schema(description = "设备id")
    private String deviceId;
    @Schema(description = "会员电话号码")
    private String phoneNumber;
    @Schema(description = "会员邮件地址")
        private String emailAddress;
    @Schema(description = "会员邀请码")
    private String iCode;
    @Schema(description = "邀请人会员id")
    private long iMemid;
    @Schema(description = "邀请人会员账号")
    private String iMemname;
    @Schema(description = "游客状态")
    private int visitorStatus;
    @Schema(description = "游客最后登录时间")
    private LocalDateTime visitorLasttime;
    @Schema(description = "游客首次登录时间")
    private LocalDateTime visitorFirsttime;
    @Schema(description = "游客到期时间")
    private LocalDateTime visitorExpiretime;
}
