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

/**
 * 基于 MongoDB 的 GoalHub 日志写入服务实现。
 */
@Service
public class GoalhubLogServiceImpl implements GoalhubLogService {

    /**
     * MongoDB 操作模板。
     */
    private final MongoTemplate mongoTemplate;

    /**
     * 当前应用服务名称。
     */
    private final String serviceName;

    /**
     * 创建日志写入服务实现。
     *
     * @param mongoTemplate MongoDB 操作模板
     * @param environment   Spring 环境配置
     */
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

    /**
     * 写入业务日志。
     *
     * @param moduleName   业务模块名称
     * @param event        业务事件名称
     * @param operatorId   操作人 ID
     * @param operatorName 操作人名称
     * @param content      业务日志内容
     */
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

    /**
     * 写入系统日志。
     *
     * @param moduleName 系统模块名称
     * @param event      系统事件名称
     * @param content    系统日志内容
     */
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

    /**
     * 写入错误日志。
     *
     * @param moduleName 错误所属模块名称
     * @param event      错误事件名称
     * @param throwable  异常对象
     */
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
