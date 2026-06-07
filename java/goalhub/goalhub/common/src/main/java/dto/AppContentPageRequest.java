package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App 内容分页查询请求。
 *
 * <p>用于 App 端分页查询已发布的运营内容。</p>
 */
@Schema(description = "App 内容分页查询请求")
@Data
public class AppContentPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex = 1;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;
}
