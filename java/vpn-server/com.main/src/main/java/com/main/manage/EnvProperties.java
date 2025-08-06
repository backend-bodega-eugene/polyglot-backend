package com.main.manage;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class EnvProperties {
    @Value("${appkey}")
    private String appKey;
    @Value("${appid}")
    private String appId;
    @Value("${trcaddress}")
    private String trcAddress;
}
