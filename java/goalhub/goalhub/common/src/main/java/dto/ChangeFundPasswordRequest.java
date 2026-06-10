package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeFundPasswordRequest {

    @NotBlank(message = "旧资金密码不能为空")
    private String oldFundPassword;

    @NotBlank(message = "新资金密码不能为空")
    private String newFundPassword;

}