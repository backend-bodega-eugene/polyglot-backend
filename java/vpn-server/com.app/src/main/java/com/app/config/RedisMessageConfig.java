package com.app.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisMessageConfig {

    @Autowired
    SubMsgListener subMsgListener;

    @Lazy
    @Autowired
    MessageListenerAdapter messageListener;

    /**
     * 消息侦听器容器
     *
     * @param factory 连接工厂
     * @return RedisMessageListenerContainer
     */
//    @Bean
//    RedisMessageListenerContainer redisContainer(final RedisConnectionFactory factory) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(factory);
//        container.addMessageListener(messageListener, new ChannelTopic("redischannel"));
//        return container;
//    }

    /**
     * 消息监听适配器
     * MessageListenerAdapter
     *
     * @return
     */
    @Bean
    MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(subMsgListener);
    }

}
