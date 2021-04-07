package com.eskeli.search.adapt;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class EntityTypeAdapter {

    /**
     * Resolve collection generic clazz
     *
     * @param t : T : 任意类型对象
     * @return
     */
    public static <T extends Collection> Class<?> resolveCollectionGenericClazz(T t) {
        Class<?> collectionGenericClazz = null;
        Type genericType = t.getClass().getGenericSuperclass();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            collectionGenericClazz = (Class<?>) pt.getActualTypeArguments()[0];
        }
        return collectionGenericClazz;
    }

}
