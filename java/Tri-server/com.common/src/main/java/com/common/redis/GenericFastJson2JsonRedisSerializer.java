package com.common.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2018/8/15 23:23
 * @description 自定义GenericFastJson2JsonRedisSerializer序列化器，其中用到了FastJsonWraper包装类
 */
public class GenericFastJson2JsonRedisSerializer<T> implements RedisSerializer<T> {
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    public GenericFastJson2JsonRedisSerializer() {
        super();
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        FastJsonWrapper<T> wrapperSet = new FastJsonWrapper<>(t);
        return JSON.toJSONString(wrapperSet, SerializerFeature.WriteClassName).getBytes(DEFAULT_CHARSET);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        String deserializeStr = new String(bytes, DEFAULT_CHARSET);
        FastJsonWrapper<T> wrapper = JSON.parseObject(deserializeStr, FastJsonWrapper.class);
        return wrapper.getValue();
    }
}