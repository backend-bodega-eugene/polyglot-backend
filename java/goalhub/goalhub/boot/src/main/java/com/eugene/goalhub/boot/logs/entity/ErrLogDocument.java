package com.eugene.goalhub.boot.logs.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("err_logs")
public class ErrLogDocument {

    @Id
    private String id;

    private String serviceName;

    private String moduleName;

    private String event;

    private String exceptionType;

    private String message;

    private String stackTrace;

    private String traceId;

    private LocalDateTime createdAt;
}