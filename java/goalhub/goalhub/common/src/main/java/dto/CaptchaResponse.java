package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 图形验证码响应。
 *
 * <p>返回验证码唯一标识和验证码图片内容，登录时需回传验证码标识。</p>
 */
@Schema(description = "图形验证码响应")
@Data
public class CaptchaResponse {

    /**
     * 验证码 key。
     */
    @Schema(description = "验证码 key", example = "captcha:9f7a6c2d")
    private String captchaKey;

    /**
     * Base64 图片内容。
     */
    @Schema(description = "Base64 图片")
    private String captchaImage;
}
