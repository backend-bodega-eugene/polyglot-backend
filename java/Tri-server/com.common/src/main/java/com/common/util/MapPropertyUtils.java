package com.common.util;

import com.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MapPropertyUtils {

    public static <T> T toObject(Map<String, Object> values, Class<T> target) {
        Object obj = null;
        try {
            obj = target.getDeclaredConstructor().newInstance();

            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (values.containsKey(key)) {
                    Object value = values.get(key);
                    Method setter = property.getWriteMethod();
                    setter.invoke(obj, value);
                }
            }
        } catch (Exception e) {
            log.error("map to object error", e);
            throw new BizException(-1, "转换map 到Object error");
        }

        return (T) obj;

    }

    public static <T> T toObject(Map<String, Object> values) {
        Object obj = null;
        try {
            Class<T> target = (Class<T>) Class.forName(values.get("classType").toString());
            obj = target.getDeclaredConstructor().newInstance();
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (values.containsKey(key)) {
                    Object value = values.get(key);
                    Method setter = property.getWriteMethod();
                    setter.invoke(obj, value);
                }
            }
        } catch (Exception e) {
            log.error("map to object error", e);
            throw new BizException(-1, "转换map 到Object error");
        }
        return (T) obj;

    }

    public static Map<String, Object> toMap(Object obj) {

        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("classType", obj.getClass().getName());
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 过滤class属性
                if (!key.equals("class")) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);
                    if (value == null) {
                        continue;
                    }
                    if (value instanceof Map) {
                        map.putAll((Map<? extends String, ?>) value);
                    } else {
                        map.put(key, value);
                    }

                }


            }
        } catch (Exception e) {
            log.error("object to map error", e);
            throw new BizException(-1, "转换map  到Object  error");
        }

        return map;

    }


}
