package com.eugene.goalhub.boot.logs.service;

public interface GoalhubLogService {

    void bizLog(
            String moduleName,
            String event,
            Long operatorId,
            String operatorName,
            String content
    );

    void sysLog(
            String moduleName,
            String event,
            String content
    );

    void errLog(
            String moduleName,
            String event,
            Throwable throwable
    );
}