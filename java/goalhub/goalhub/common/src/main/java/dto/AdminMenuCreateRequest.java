package dto;

import lombok.Data;

@Data
public class AdminMenuCreateRequest {

    private Long parentId;

    private String name;

    private Integer type;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer status;
}