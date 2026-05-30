package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogQueryRequest {

    private Integer pageIndex = 1;

    private Integer pageSize = 10;

    private String serviceName;

    private String moduleName;

    private String event;

    private String operatorName;

    private LocalDateTime createdAtStart;

    private LocalDateTime createdAtEnd;
}