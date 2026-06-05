package com.eugene.goalhub.boot.logs.service.impl;


import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import com.eugene.goalhub.boot.logs.service.GoalhubLogService;
import jakarta.annotation.PreDestroy;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 基于 MongoDB 的 GoalHub 日志写入服务实现。
 *
 * <p>日志写入通过独立线程池异步执行，调用方只负责提交日志任务，不等待 MongoDB 写入结果。</p>
 */
@Service
public class GoalhubLogServiceImpl implements GoalhubLogService {

    /**
     * 日志线程池关闭等待秒数。
     */
    private static final long SHUTDOWN_WAIT_SECONDS = 3L;

    /**
     * MongoDB 操作模板。
     */
    private final MongoTemplate mongoTemplate;

    /**
     * 日志异步写入线程池。
     */
    private final ExecutorService logExecutor;

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
        this.logExecutor = createLogExecutor();
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
        submitLogTask(() -> saveBizLog(moduleName, event, operatorId, operatorName, content));
    }

    /**
     * 写入业务日志文档。
     *
     * @param moduleName   业务模块名称
     * @param event        业务事件名称
     * @param operatorId   操作人 ID
     * @param operatorName 操作人名称
     * @param content      业务日志内容
     */
    private void saveBizLog(
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

        mongoTemplate.save(log);
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
        submitLogTask(() -> saveSysLog(moduleName, event, content));
    }

    /**
     * 写入系统日志文档。
     *
     * @param moduleName 系统模块名称
     * @param event      系统事件名称
     * @param content    系统日志内容
     */
    private void saveSysLog(
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

        mongoTemplate.save(log);
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
        submitLogTask(() -> saveErrLog(moduleName, event, throwable));
    }

    /**
     * 写入错误日志文档。
     *
     * @param moduleName 错误所属模块名称
     * @param event      错误事件名称
     * @param throwable  异常对象
     */
    private void saveErrLog(
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

        mongoTemplate.save(log);
    }

    /**
     * 提交异步日志任务。
     *
     * <p>提交失败或执行失败只影响日志自身，不向业务调用方抛出异常。</p>
     *
     * @param task 日志任务
     */
    private void submitLogTask(Runnable task) {
        try {
            logExecutor.execute(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建日志异步写入线程池。
     *
     * @return 日志线程池
     */
    private ExecutorService createLogExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(10000),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("goalhub-log-writer");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardPolicy()
        );
    }

    /**
     * 关闭日志线程池。
     */
    @PreDestroy
    public void shutdown() {
        logExecutor.shutdown();
        try {
            if (!logExecutor.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                logExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logExecutor.shutdownNow();
        }
    }
}
