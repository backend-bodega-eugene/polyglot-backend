package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetFundPasswordRequest {

    @NotBlank(message = "资金密码不能为空")
    private String fundPassword;

}