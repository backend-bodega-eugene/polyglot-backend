package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("vpn_linestatus")
public class VpnLinestatus extends AbstractEntity {
    @Schema(description = "会员id")
    private long memId;
    @Schema(description = "会员名称")
    private String memName;
    @Schema(description = "线路名称")
    private String lineName;
    @Schema(description = "线路地址")
    private String onlineIpaddress;
    @Schema(description = "在线状态")
    private Integer onlineStatus;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "最后在线状态更新时间")
    private LocalDateTime lastUpdatetime;
}
