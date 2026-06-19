package com.eugene.goalhub.order.service;

import dto.AdminMatchResultJudgeRequest;

/**
 * 后台订单系统预判服务。
 */
public interface AdminOrderResultService {

    /**
     * 根据赛事赛果生成订单系统预判结果。
     *
     * @param request 赛事订单系统预判请求
     */
    void judgeMatch(AdminMatchResultJudgeRequest request);
}