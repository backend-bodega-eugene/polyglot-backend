package com.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FieldUtils {
    /**
     * 获取本类及其父类的属性的方法
     *
     * @param clazz 当前类对象
     * @return 字段数组
     */
    private static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null) {
            fieldList.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
            clazz = clazz.getSuperclass();
        }
        Field[] fields = new Field[fieldList.size()];
        return fieldList.toArray(fields);
    }
}
