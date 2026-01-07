package com.common.util;

import com.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class BeanCoper {
    /**
     * 对象copy
     *
     * @param desc   目标对象
     * @param source 源对象
     */
    public static void copyProperties(Object desc, Object source) {
        BeanUtils.copyProperties(source, desc);
    }


    /**
     * 对象copy
     *
     * @param descType 目标类型
     * @param source   源对象
     * @param <T>
     * @return
     */
    public static <T> T clone(Class<T> descType, Object source) {
        return copyProperties(descType, source);
    }

    /**
     * copy对象
     *
     * @param descType   目标类型
     * @param listSource 源类型
     * @param <T>
     * @return
     */
    public static <T> List<T> clone(Class<T> descType, List listSource) {
        List resultList = new ArrayList();
        listSource.forEach((e) -> {
            resultList.add(copyProperties(descType, e));
        });
        return resultList;
    }

    /**
     * 分页数据copy
     *
     * @param descType   目标类型
     * @param sourcePage 源类型
     * @param <T>
     * @return
     */
    public static <T> Page<T> clone(Class<T> descType, Page sourcePage) {
        List<T> resultList = new ArrayList<>();
        sourcePage.getContent().forEach((e) -> {
            resultList.add(copyProperties(descType, e));
        });
        return new PageImpl(resultList, sourcePage.getPageable(), sourcePage.getTotalElements());
    }

    /**
     * List copy
     *
     * @param descType   目标类型
     * @param listSource 源类型
     * @param <T>
     * @return
     */
    public static <T> List<T> copyList(Class<T> descType, List listSource) {
        List resultList = new ArrayList();
        listSource.forEach((e) -> {
            resultList.add(copyProperties(descType, e));
        });
        return resultList;
    }


    /**
     * 分页数据copy
     *
     * @param descType   目标类型
     * @param sourcePage 源类型
     * @param <T>
     * @return
     */
    public static <T> Page<T> copyPage(Class<T> descType, Page sourcePage) {
        List<T> resultList = new ArrayList<>();
        sourcePage.getContent().forEach((e) -> {
            resultList.add(copyProperties(descType, e));
        });
        return new PageImpl(resultList, sourcePage.getPageable(), sourcePage.getTotalElements());
    }

    /**
     * 对象copy
     *
     * @param descType 目标类型
     * @param source   源对象
     * @param <T>
     * @return
     */
    public static <T> T copyProperties(Class<T> descType, Object source) {
        if (descType == null) {
            throw new ApplicationException("descType.error");
        }
        T entity = null;
        try {
            entity = (T) descType.getConstructor().newInstance();
        } catch (Exception e) {
            throw new ApplicationException(descType.getSimpleName() + "Constructors.error", e);
        }
        if (source == null) {
            return entity;
        }
        copyProperties(entity, source);
        return entity;

    }
}
