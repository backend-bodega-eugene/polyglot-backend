package com.app.controller.dto.responsedto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema
public class VpnMemberResponseVOextend {
    @Schema(description = "会员名称")
    private String memName;
    @Schema(description = "会员邀请码")
    private String iCode;
    @Schema(description = "邀请人会员id")
    private String iMemid;
    @Schema(description = "邀请人会员账号")
    private String iMemname;
    @Schema(description = "会员状态")
    private Integer memStatus;
    @Schema(description = "是否会员")
    private Integer isMember;
    @Schema(description = "到期时间 暂时不用")
    private LocalDateTime expirTime;
    @Schema(description = "允许客户端数,默认1")
    private Integer allowClient;
    @Schema(description = "trc交易地址")
    private String trcAddress;
    @Schema(description = "trc交易地址2")
    private String memGuid;
    @Schema(description = "会员id")
    private long id;
    //    @Schema(description = "登录秘钥,验证是否登录的凭据,请在需要验证登录的接口header中放入  userSession:loginKey")
//    private String loginKey;
    @Schema(description = "0,会员,1,游客")
    @TableField(exist = false)
    private int isVisiter;
    @Schema(description = "用户uid")
    private String uid;
}
