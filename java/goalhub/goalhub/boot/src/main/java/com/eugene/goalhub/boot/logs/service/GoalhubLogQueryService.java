package com.eugene.goalhub.boot.logs.service;

import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import dto.LogQueryRequest;
import dto.PageResponse;

/**
 * GoalHub 日志查询服务。
 */
public interface GoalhubLogQueryService {

    /**
     * 分页查询业务日志。
     *
     * @param request 日志查询参数
     * @return 业务日志分页结果
     */
    PageResponse<BizLogDocument> queryBizLogs(LogQueryRequest request);

    /**
     * 分页查询系统日志。
     *
     * @param request 日志查询参数
     * @return 系统日志分页结果
     */
    PageResponse<SysLogDocument> querySysLogs(LogQueryRequest request);

    /**
     * 分页查询错误日志。
     *
     * @param request 日志查询参数
     * @return 错误日志分页结果
     */
    PageResponse<ErrLogDocument> queryErrLogs(LogQueryRequest request);
}
