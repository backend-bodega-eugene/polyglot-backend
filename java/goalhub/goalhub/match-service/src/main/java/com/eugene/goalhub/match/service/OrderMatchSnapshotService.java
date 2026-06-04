package com.eugene.goalhub.match.service;

import dto.OrderMatchOptionSnapshotResponse;

public interface OrderMatchSnapshotService {

    OrderMatchOptionSnapshotResponse getOrderSnapshot(
            Long matchMarketOptionId);
}