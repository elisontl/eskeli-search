package com.eskeli.search.center.engine.idxunits;

import com.eskeli.search.annotation.KeliSearchIdxEntity;
import com.eskeli.search.annotation.KeliSearchIdxArea;
import com.eskeli.search.annotation.KeliSearchNotIdxArea;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Class Desc : 索引域识别与合成组件
 *
 * @author elison.s
 */
public class IndexFieldIdentifyComponent {

    /**
     * 合成索引域
     * @param clazz : Class<?> : 索引类对象
     * @return
     */
    public static Field[] synthesizeIndexFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        // 是否为全量索引
        KeliSearchIdxEntity indexClazz = clazz.getDeclaredAnnotation(KeliSearchIdxEntity.class);
        // 全域索引
        if (indexClazz == null || indexClazz.wholeAreaIndex()) {
            // ( 个人观点 ): Jackson Api 设计的缺陷，Gson Api的适配设计特点，indexClazz == null 场景也开放出来，更加灵活
             return Arrays.stream(fields)
                .filter(field -> field.getDeclaredAnnotation(KeliSearchNotIdxArea.class) == null)
                .toArray(value -> new Field[value]);
        }
        // 非全域索引
        else {
            return Arrays.stream(fields)
                .filter(field -> field.getDeclaredAnnotation(KeliSearchIdxArea.class) != null)
                .filter(field -> field.getDeclaredAnnotation(KeliSearchNotIdxArea.class) == null)
                .toArray(value -> new Field[value]);
        }
    }

    /**
     * 获取索引域名称集合，不同名称间以逗号分隔
     * @param clazz : Class<?> : 索引类对象
     * @return
     */
    public static String synthesizeIndexFieldNames(Class<?> clazz) {
        Field[] fields = synthesizeIndexFields(clazz);
        StringBuffer indexFieldNames = new StringBuffer("");
        Arrays.stream(fields).forEach(field -> {
            indexFieldNames.append(field.getName()).append(",");
        });
        return indexFieldNames.length() > 1
                ? indexFieldNames.toString().substring(0, indexFieldNames.length() - 1) : indexFieldNames.toString();
    }

    /**
     * 获取索引域名称数组
     * @param clazz : Class<?> : 索引类对象
     * @return
     */
    public static String[] synthesizeIndexFieldNameArray(Class<?> clazz) {
        Field[] fields = synthesizeIndexFields(clazz);
        StringBuffer indexFieldNames = new StringBuffer("");
        return Arrays.stream(fields)
            .map(fieldName -> fieldName.getName()).toArray(value -> new String[value]);
    }

}
