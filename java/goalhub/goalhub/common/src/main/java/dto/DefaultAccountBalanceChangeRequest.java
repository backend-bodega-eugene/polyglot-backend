package dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DefaultAccountBalanceChangeRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Digits(integer = 18, fraction = 2, message = "金额最多保留2位小数")
    private BigDecimal amount;

    @NotBlank(message = "业务ID不能为空")
    private String bizId;

    private String remark;
}