package com.main.config;

import com.alibaba.fastjson.JSON;
import com.common.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PubMsgTask {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    public void PubMemberStatus(String memberStatus) {

        redisTemplate.convertAndSend("redischannel", memberStatus);
    }
}