package com.app.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class IdDtovpnpacka {
    @Schema(description = "vpnpackageid")
    private Long vpnpackageid;
}
