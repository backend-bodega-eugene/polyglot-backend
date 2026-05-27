package dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminMenuTreeResponse {

    private Long id;

    private Long parentId;

    private String name;

    private Integer type;

    private String path;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    private List<AdminMenuTreeResponse> children = new ArrayList<>();
}