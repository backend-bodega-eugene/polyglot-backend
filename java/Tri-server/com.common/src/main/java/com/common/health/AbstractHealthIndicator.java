package com.common.health;

import com.common.rpc.StatusRpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

@Slf4j
public abstract class AbstractHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        if (getStatusRpcService().status()) {
            return new Health.Builder().up().build();
        } else {
            return new Health.Builder().down().build();
        }
    }

    /**
     * 返回抽像服务RPC接口
     *
     * @return
     */
    protected abstract StatusRpcService getStatusRpcService();


}
