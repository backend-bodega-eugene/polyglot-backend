package com.app.manage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema
public class RefLocalDateTime {
    private LocalDateTime myLocalDateTime;
}
