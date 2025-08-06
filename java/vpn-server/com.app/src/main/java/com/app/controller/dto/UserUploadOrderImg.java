package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class UserUploadOrderImg {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "用户上传凭证图片")
    private String userImage;
}
