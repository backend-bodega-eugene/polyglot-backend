package com.main.manage.responseVO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema
public class AllOrderSum {
    private BigDecimal handingOrderSum;
}
