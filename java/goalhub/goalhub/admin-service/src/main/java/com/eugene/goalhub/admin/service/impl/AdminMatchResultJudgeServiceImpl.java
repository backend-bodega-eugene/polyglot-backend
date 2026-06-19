package com.eugene.goalhub.admin.service.impl;

import com.eugene.goalhub.admin.client.AdminMatchResultFeignClient;
import com.eugene.goalhub.admin.client.AdminOrderResultFeignClient;
import com.eugene.goalhub.admin.service.AdminMatchResultJudgeService;
import com.eugene.goalhub.admin.service.support.FeignResultSupport;
import dto.AdminMatchResultJudgeRequest;
import dto.SaveMatchResultRequest;
import org.springframework.stereotype.Service;

/**
 * 后台赛事赛果与订单系统预判编排服务实现。
 *
 * <p>
 * 当前服务负责后台编排：
 * 先调用 match-service 保存赛事赛果并改变赛事状态，
 * 再调用 order-service 根据赛果生成订单 system_result 与预期金额。
 * </p>
 */
@Service
public class AdminMatchResultJudgeServiceImpl
        implements AdminMatchResultJudgeService {

    /**
     * 后台比赛结果远程调用客户端。
     */
    private final AdminMatchResultFeignClient adminMatchResultFeignClient;

    /**
     * 后台订单系统预判远程调用客户端。
     */
    private final AdminOrderResultFeignClient adminOrderResultFeignClient;

    /**
     * 创建后台赛事赛果与订单系统预判编排服务实现。
     *
     * @param adminMatchResultFeignClient 后台比赛结果远程调用客户端
     * @param adminOrderResultFeignClient 后台订单系统预判远程调用客户端
     */
    public AdminMatchResultJudgeServiceImpl(
            AdminMatchResultFeignClient adminMatchResultFeignClient,
            AdminOrderResultFeignClient adminOrderResultFeignClient) {

        this.adminMatchResultFeignClient =
                adminMatchResultFeignClient;
        this.adminOrderResultFeignClient =
                adminOrderResultFeignClient;
    }

    /**
     * 保存赛事赛果，并生成该赛事相关订单的系统预判结果。
     *
     * @param request       赛果保存参数
     * @param adminId       管理员 ID
     * @param adminUsername 管理员用户名
     */
    @Override
    public void saveResultAndJudgeOrders(
            SaveMatchResultRequest request,
            Long adminId,
            String adminUsername) {

        FeignResultSupport.checkSuccess(
                adminMatchResultFeignClient.save(request)
        );

        AdminMatchResultJudgeRequest judgeRequest =
                new AdminMatchResultJudgeRequest();

        judgeRequest.setMatchId(request.getMatchId());
        judgeRequest.setRemark("后台设置赛果后生成订单系统预判结果");
        judgeRequest.setAdminId(adminId);
        judgeRequest.setAdminUsername(adminUsername);
        judgeRequest.setMatchResult(request);
        FeignResultSupport.checkSuccess(
                adminOrderResultFeignClient.judgeMatch(judgeRequest)
        );
    }
}