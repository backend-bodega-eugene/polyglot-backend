package com.main.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application_info")
public class ApplicationInfo extends AbstractEntity {
    @Schema(description = "客户端名称")
    private String applicationName;
    @Schema(description = "客户端图标")
    private String applicationIcon;
    @Schema(description = "客户端url")
    private String applicationUrl;
    @Schema(description = "客户端地址")
    private String applicationAddress;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "客户端版本")
    private String applicationVersion;
    @Schema(description = "是否强制更新")
    private Integer applicationForceupdate;
    @Schema(description = "客户端是否可用")
    private Integer applicationStatus;
    @Schema(description = "客户端类型 0 Android,1  ios,2 pc,3 mac,4 linux")
    private Integer applicationType;
}
