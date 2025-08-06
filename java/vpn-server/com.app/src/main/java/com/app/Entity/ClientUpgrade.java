package com.app.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("client_upgrade")
public class ClientUpgrade extends AbstractEntity {

    @Schema(description = "版本名称")
    private String versionName;
    @Schema(description = "客户端")
    private String clientName;
    @Schema(description = "版本号")
    private String versionNumber;
    @Schema(description = "下载地址")
    private String downloadAddress;
    @Schema(description = "是否强制更新")
    private Integer forceUpdate;
    @Schema(description = "是否启用,默认启用0,不启用1")
    private Integer status;
    @Schema(description = "更新提示语")
    private String remark;
    @Schema(description = "版本类型0:ios,1:android,2:windows,3:macOS,4:Linux")
    private String versionType;
}
