package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 通用分页响应。
 *
 * @param <T> 分页记录类型
 */
@Schema(description = "通用分页响应")
@Data
public class PageResponse<T> {

    /**
     * 创建空分页响应。
     */
    public PageResponse() {}

    /**
     * 创建分页响应。
     *
     * @param total     总记录数
     * @param pageIndex 当前页码
     * @param pageSize  每页数量
     * @param records   当前页记录列表
     */
    public PageResponse(Long total,Integer pageIndex,Integer pageSize,List<T>  records) {
        this.total = total;
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.records = records;
    }
    /**
     * 总记录数。
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 当前页码。
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    /**
     * 当前页记录列表。
     */
    @Schema(description = "当前页记录列表")
    private List<T> records;
}
