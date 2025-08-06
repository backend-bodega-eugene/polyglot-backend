package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vpn_member")
public class VpnMember extends AbstractEntity {
    @Schema(description = "会员名称")
    private String memName;
    @Schema(description = "会员密码")
    private String memPassword;
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
    @Schema(description = "会员状态")
    private Integer memStatus;
    @Schema(description = "是否会员")
    private Integer isMember;
    @Schema(description = "到期时间 暂时不用")
    private LocalDateTime expirTime;
    @Schema(description = "允许客户端数,默认1")
    private Integer allowClient;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLogintime;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "trc交易地址")
    private String trcAddress;
    @Schema(description = "trc交易地址2")
    private String memGuid;
    @Schema(description = "上级邀请码")
    private String fatherIcode;
    @Schema(description = "用户类型 ,0会员,1,游客")
    private Integer memType;
    @Schema(description = "注册地址")
    private String uid;
    @Schema(description = "用户uid")
    private String registIpaddress;
    @Schema(description = "在线情况")
    @TableField(exist = false)
    private String userOnlineStatus;

}