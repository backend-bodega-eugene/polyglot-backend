//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.util;

import com.common.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class GlossaryEnumUtils {
    private static Map<String, Class> classes = new HashMap();
    private Map<String, String> enumTypes;

    public static void setClasses(String shortName, String className) {
        Class name = null;
        try {
            name = Class.forName(className);
            if (!IGlossary.class.isAssignableFrom(name)) {
                throw new ApplicationException(name.getName() + "must be extend or implements  from IGlossary");
            }
            if (classes.containsKey(shortName)) {
                return;
            }
            if (classes.containsValue(name)) {
                return;
            }
        } catch (ClassNotFoundException var4) {
            throw new ApplicationException("init Spring Bean EnumUtil error", var4);
        }

        classes.put(shortName, name);
    }

    public Map<String, String> getEnumTypes() {
        return this.enumTypes;
    }

    public void setEnumTypes(Map<String, String> enumTypes) {
        this.enumTypes = enumTypes;
    }

    public void init() {
        Iterator items = this.enumTypes.keySet().iterator();

        while (items.hasNext()) {
            String name = (String) items.next();
            setClasses(name, (String) this.enumTypes.get(name));
        }

    }

    public void AddItem(String name, Class<? extends IGlossary> clazz) {
        classes.put(name, clazz);
    }

    public static <T extends IGlossary> List<T> getItems(Class<T> clazz) {
        if (clazz == null) {
            throw new ApplicationException("class can not be null");
        } else if (!clazz.isEnum()) {
            throw new ApplicationException(clazz.getName() + "must be Enum type.");
        } else if (!IGlossary.class.isAssignableFrom(clazz)) {
            throw new ApplicationException(clazz.getName() + "must be extend or implements  from IGlossary");
        } else {
            IGlossary[] enumConstants = (IGlossary[]) clazz.getEnumConstants();
            ArrayList list = new ArrayList();
            Collections.addAll(list, enumConstants);
            return list;
        }
    }

    public static <T extends IGlossary> List<T> getItemsByShortName(String shortName) {
        Class clazz = (Class) classes.get(shortName);
        if (clazz == null) {
            throw new ApplicationException("缺少枚举配置,类型  +" + shortName + "");
        } else {
            return getItems(clazz);
        }
    }

    public static <T extends IGlossary> T getItem(Class<T> clazz, String name) {
        List<T> items = getItems(clazz);
        for (T t : items) {
            if (t instanceof Enum) {
                Enum e = (Enum) t;
                if (e.name().equals(name)) {
                    return t;
                }
            }
        }
        return null;
    }
}
