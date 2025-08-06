package com.main.manage.responseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema
public class PayTypeResponseVO {
    @Schema(description = "支付类型id值")
    private Long value;
    @Schema(description = "支付类型文字")
    private String label;
    private List<PayInfoResponseVO> children;
}
