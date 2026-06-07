package dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppDepositOrderCreateRequest {

    @DecimalMin(value = "0.01", message = "充值金额必须大于0")
    @Digits(integer = 18, fraction = 2, message = "充值金额最多保留2位小数")
    private BigDecimal amount;

    private String currencyCode;

    private String chainType;

    private String txHash;

    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}