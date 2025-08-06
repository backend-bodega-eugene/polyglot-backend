package com.common.rpc;

import com.common.interfaces.ResultStatus;

/**
 * rpc服务监存活状态
 */
public interface StatusRpcService extends ResultStatus {
    /**
     * 存活状态
     *
     * @return
     */
    Boolean status();
}
