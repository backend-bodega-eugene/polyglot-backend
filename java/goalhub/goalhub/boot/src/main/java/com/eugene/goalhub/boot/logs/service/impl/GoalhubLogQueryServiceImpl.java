package com.eugene.goalhub.boot.logs.service.impl;

import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import com.eugene.goalhub.boot.logs.service.GoalhubLogQueryService;
import dto.LogQueryRequest;
import dto.PageResponse;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于 MongoDB 的 GoalHub 日志查询服务实现。
 */
@Service
public class GoalhubLogQueryServiceImpl implements GoalhubLogQueryService {

    /**
     * MongoDB 操作模板。
     */
    private final MongoTemplate mongoTemplate;

    /**
     * 创建日志查询服务实现。
     *
     * @param mongoTemplate MongoDB 操作模板
     */
    public GoalhubLogQueryServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 分页查询业务日志。
     *
     * @param request 日志查询参数
     * @return 业务日志分页结果
     */
    @Override
    public PageResponse<BizLogDocument> queryBizLogs(LogQueryRequest request) {
        return query(request, BizLogDocument.class, "biz_logs");
    }

    /**
     * 分页查询系统日志。
     *
     * @param request 日志查询参数
     * @return 系统日志分页结果
     */
    @Override
    public PageResponse<SysLogDocument> querySysLogs(LogQueryRequest request) {
        return query(request, SysLogDocument.class, "sys_logs");
    }

    /**
     * 分页查询错误日志。
     *
     * @param request 日志查询参数
     * @return 错误日志分页结果
     */
    @Override
    public PageResponse<ErrLogDocument> queryErrLogs(LogQueryRequest request) {
        return query(request, ErrLogDocument.class, "err_logs");
    }

    /**
     * 按集合名称分页查询日志。
     *
     * @param request        日志查询参数
     * @param clazz          返回文档类型
     * @param collectionName MongoDB 集合名称
     * @param <T>            日志文档类型
     * @return 日志分页结果
     */
    private <T> PageResponse<T> query(LogQueryRequest request, Class<T> clazz, String collectionName) {
        int pageIndex = request.getPageIndex() == null || request.getPageIndex() < 1
                ? 1
                : request.getPageIndex();

        int pageSize = request.getPageSize() == null || request.getPageSize() < 1
                ? 10
                : request.getPageSize();

        Query query = buildQuery(request);

        long total = mongoTemplate.count(query, collectionName);

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        query.skip((long) (pageIndex - 1) * pageSize);
        query.limit(pageSize);

        List<T> records = mongoTemplate.find(query, clazz, collectionName);

        return new PageResponse<>(total, pageIndex, pageSize, records);
    }

    /**
     * 根据查询请求构造 MongoDB 查询条件。
     *
     * @param request 日志查询参数
     * @return MongoDB 查询对象
     */
    private Query buildQuery(LogQueryRequest request) {
        List<Criteria> criteriaList = new ArrayList<>();

        if (hasText(request.getServiceName())) {
            criteriaList.add(Criteria.where("serviceName").is(request.getServiceName()));
        }

        if (hasText(request.getModuleName())) {
            criteriaList.add(Criteria.where("moduleName").regex(request.getModuleName(), "i"));
        }

        if (hasText(request.getEvent())) {
            criteriaList.add(Criteria.where("event").regex(request.getEvent(), "i"));
        }

        if (hasText(request.getOperatorName())) {
            criteriaList.add(Criteria.where("operatorName").regex(request.getOperatorName(), "i"));
        }

        if (request.getCreatedAtStart() != null || request.getCreatedAtEnd() != null) {
            Criteria createdAt = Criteria.where("createdAt");

            if (request.getCreatedAtStart() != null) {
                createdAt.gte(request.getCreatedAtStart());
            }

            if (request.getCreatedAtEnd() != null) {
                createdAt.lte(request.getCreatedAtEnd());
            }

            criteriaList.add(createdAt);
        }

        Query query = new Query();

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        return query;
    }

    /**
     * 判断字符串是否包含有效文本。
     *
     * @param value 待判断字符串
     * @return 包含有效文本返回 true，否则返回 false
     */
    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
