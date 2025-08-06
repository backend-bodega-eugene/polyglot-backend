package com.main.manage.requestbodyvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class EditArticleProtocolVO {
    @Schema(description = "用户注册协议")
    private String userProtocal;
    @Schema(description = "隐私协议")
    private String userPrivate;
    @Schema(description = "奖励规则")
    private String rewardRules;

}
