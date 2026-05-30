package com.eugene.goalhub.boot.logs.service;

import com.eugene.goalhub.boot.logs.entity.BizLogDocument;
import com.eugene.goalhub.boot.logs.entity.ErrLogDocument;
import com.eugene.goalhub.boot.logs.entity.SysLogDocument;
import dto.LogQueryRequest;
import dto.PageResponse;

public interface GoalhubLogQueryService {

    PageResponse<BizLogDocument> queryBizLogs(LogQueryRequest request);

    PageResponse<SysLogDocument> querySysLogs(LogQueryRequest request);

    PageResponse<ErrLogDocument> queryErrLogs(LogQueryRequest request);
}