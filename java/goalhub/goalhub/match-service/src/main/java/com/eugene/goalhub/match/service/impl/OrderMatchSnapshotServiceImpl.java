package com.eugene.goalhub.match.service.impl;

import com.eugene.goalhub.match.mapper.OrderMatchSnapshotMapper;
import com.eugene.goalhub.match.service.OrderMatchSnapshotService;
import com.eugene.goalhub.match.service.support.MatchOperationLogger;
import dto.OrderMatchOptionSnapshotResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

/**
 * 订单赛事快照查询服务实现。
 *
 * <p>负责为订单服务查询下单时使用的赛事玩法赔率快照，并处理空参数和空结果。</p>
 */
@Service
public class OrderMatchSnapshotServiceImpl
        implements OrderMatchSnapshotService {

    /**
     * 系统日志模块名称。
     */
    private static final String MODULE_NAME = "订单赛事快照查询";

    /**
     * 默认快照语言编码。
     */
    private static final String DEFAULT_LANG_CODE = "zh-CN";

    /**
     * 订单赛事快照 Mapper。
     */
    private final OrderMatchSnapshotMapper orderMatchSnapshotMapper;

    /**
     * 比赛服务操作日志工具。
     */
    private final MatchOperationLogger matchOperationLogger;

    /**
     * 创建订单赛事快照查询服务实现。
     *
     * @param orderMatchSnapshotMapper 订单赛事快照 Mapper
     */
    public OrderMatchSnapshotServiceImpl(
            OrderMatchSnapshotMapper orderMatchSnapshotMapper,
            MatchOperationLogger matchOperationLogger) {
        this.orderMatchSnapshotMapper = orderMatchSnapshotMapper;
        this.matchOperationLogger = matchOperationLogger;
    }

    /**
     * 查询下单用赛事玩法赔率快照。
     *
     * @param matchMarketOptionId 赛事玩法赔率 ID
     * @return 下单用赛事玩法赔率快照
     */
    @Override
    public OrderMatchOptionSnapshotResponse getOrderSnapshot(
            Long matchMarketOptionId) {

        if (matchMarketOptionId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        OrderMatchOptionSnapshotResponse snapshot =
                orderMatchSnapshotMapper.selectOrderSnapshot(
                        matchMarketOptionId,
                        DEFAULT_LANG_CODE
                );

        if (snapshot == null) {
            throw new BusinessException(ResultCode.MATCH_MARKET_OPTION_NOT_FOUND);
        }

        matchOperationLogger.sysLog(
                MODULE_NAME,
                "GET_ORDER_SNAPSHOT",
                "查询订单赛事快照成功，matchMarketOptionId=" + matchMarketOptionId
        );
        return snapshot;
    }
}
