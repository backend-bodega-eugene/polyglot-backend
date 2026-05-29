package com.eugene.goalhub.boot.logs.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("sys_logs")
public class SysLogDocument {

    @Id
    private String id;

    private String serviceName;

    private String moduleName;

    private String event;

    private String content;

    private LocalDateTime createdAt;
}