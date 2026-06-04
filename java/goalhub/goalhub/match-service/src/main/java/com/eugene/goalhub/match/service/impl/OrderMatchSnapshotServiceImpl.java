package com.eugene.goalhub.match.service.impl;

import com.eugene.goalhub.match.mapper.OrderMatchSnapshotMapper;
import com.eugene.goalhub.match.service.OrderMatchSnapshotService;
import dto.OrderMatchOptionSnapshotResponse;
import exception.BusinessException;
import org.springframework.stereotype.Service;
import response.ResultCode;

@Service
public class OrderMatchSnapshotServiceImpl
        implements OrderMatchSnapshotService {

    private static final String DEFAULT_LANG_CODE = "zh-CN";

    private final OrderMatchSnapshotMapper orderMatchSnapshotMapper;

    public OrderMatchSnapshotServiceImpl(
            OrderMatchSnapshotMapper orderMatchSnapshotMapper) {
        this.orderMatchSnapshotMapper = orderMatchSnapshotMapper;
    }

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

        return snapshot;
    }
}