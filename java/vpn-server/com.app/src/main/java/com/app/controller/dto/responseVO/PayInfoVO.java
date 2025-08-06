package com.app.controller.dto.responseVO;

import com.app.Entity.PayInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema
public class PayInfoVO {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "类型名称")
    private String typeName;
    @Schema(description = "类型编码(忽略)")
    private String typeCode;
    @Schema(description = "支付url")
    private String typeUrl;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "支付通道")
    private List<PayInfo> payInfoList;
}
