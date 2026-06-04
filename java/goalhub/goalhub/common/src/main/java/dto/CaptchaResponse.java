package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "图形验证码响应")
@Data
public class CaptchaResponse {

    @Schema(description = "验证码 key")
    private String captchaKey;

    @Schema(description = "Base64 图片")
    private String captchaImage;
}