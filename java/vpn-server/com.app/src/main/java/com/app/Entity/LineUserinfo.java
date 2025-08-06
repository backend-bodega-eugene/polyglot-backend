package com.app.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mybatisplus.AbstractEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("line_userinfo")
public class LineUserinfo extends AbstractEntity {
    @Schema(description = "支付类型")
    private Long lineId;
    @Schema(description = "支付类型")
    private Long memId;
    @Schema(description = "支付类型")
    private String remark;
}
