package com.main.job;

import com.main.service.VpnOrderinfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OntimeUpdateOrderStatus {
    @Autowired
    VpnOrderinfoService vpnOrderinfoService;

    /**
     * 定时操作订单,1分钟一次,
     */
    @Scheduled(fixedRate = 60000)
    public void backRedPackets() {
        vpnOrderinfoService.updateOrderStatus();
    }
}
