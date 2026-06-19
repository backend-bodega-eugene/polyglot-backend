package com.eugene.goalhub.admin.service;

import dto.SaveMatchResultRequest;

/**
 * 后台赛事赛果与订单系统预判编排服务。
 */
public interface AdminMatchResultJudgeService {

    /**
     * 保存赛事赛果，并生成该赛事相关订单的系统预判结果。
     *
     * @param request       赛果保存参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    void saveResultAndJudgeOrders(
            SaveMatchResultRequest request,
            Long adminId,
            String adminUsername);
}