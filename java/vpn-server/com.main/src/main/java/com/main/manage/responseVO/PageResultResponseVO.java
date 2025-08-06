package com.main.manage.responseVO;

import com.common.result.Result;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema
public class PageResultResponseVO<T> extends Result {
    @Schema(description = "页码(第几页)")
    private long pageIndex;
    @Schema(description = "每页大小")
    private long pageSize;
    @Schema(description = "每页大小")
    private long total;
}
