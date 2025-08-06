package com.app.controller.dto.responseVO;

import com.app.Entity.PayInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema
public class PayInfosResonseVO {
    private Long id;
    private String typeName;
    private String typeCode;
    private String typeUrl;
    private String remark;
    private LocalDateTime createTime;
    private List<PayInfo> payInfoList;
}
