package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class IdDto {
    @Schema(description = "memid")
    private Long memid;
}
