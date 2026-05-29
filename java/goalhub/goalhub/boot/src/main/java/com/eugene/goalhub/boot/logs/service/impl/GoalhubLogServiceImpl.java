package com.eugene.goalhub.boot.logs.service.impl;


import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Service
public class GoalhubLogServiceImpl implements GoalhubLogService {

    private final MongoTemplate mongoTemplate;
    private final String serviceName;

    public GoalhubLogServiceImpl(
            MongoTemplate mongoTemplate,
            Environment environment
    ) {
        this.mongoTemplate = mongoTemplate;
        this.serviceName = environment.getProperty(
                "spring.application.name",
                "unknown-service"
        );
    }

    @Override
    public void bizLog(
            String moduleName,
            String event,
            Long operatorId,
            String operatorName,
            String content
    ) {

        BizLogDocument log = new BizLogDocument();

        log.setServiceName(serviceName);
        log.setModuleName(moduleName);
        log.setEvent(event);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setContent(content);
        log.setCreatedAt(LocalDateTime.now());

        try {
            mongoTemplate.save(log);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sysLog(
            String moduleName,
            String event,
            String content
    ) {

        SysLogDocument log = new SysLogDocument();

        log.setServiceName(serviceName);
        log.setModuleName(moduleName);
        log.setEvent(event);
        log.setContent(content);
        log.setCreatedAt(LocalDateTime.now());

        try {
            mongoTemplate.save(log);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void errLog(
            String moduleName,
            String event,
            Throwable throwable
    ) {

        ErrLogDocument log = new ErrLogDocument();

        log.setServiceName(serviceName);
        log.setModuleName(moduleName);
        log.setEvent(event);

        log.setExceptionType(
                throwable.getClass().getName()
        );

        log.setMessage(
                throwable.getMessage()
        );

        StringWriter sw = new StringWriter();

        throwable.printStackTrace(
                new PrintWriter(sw)
        );

        log.setStackTrace(
                sw.toString()
        );

        log.setCreatedAt(LocalDateTime.now());

        try {
            mongoTemplate.save(log);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}