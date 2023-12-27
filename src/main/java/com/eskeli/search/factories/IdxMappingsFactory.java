package com.eskeli.search.factories;

import com.eskeli.search.annotation.KeliSearchIdxArea;
import org.elasticsearch.common.xcontent.XContentBuilder;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Class Desc : 索引映射工厂
 *
 * @author elisontl
 */
public class IdxMappingsFactory {

    /**
     * 生成扩展索引Mapping映射
     *
     * @param contentBuilder : XContentBuilder : 内容构建Obj
     * @param field : Field : 索引域
     * @param indexField : 索引注解O
     * @param t : T : 实体对象
     * @return
     */
    public static <T> XContentBuilder generateExtendIndexMappings(XContentBuilder contentBuilder, Field field, KeliSearchIdxArea indexField, T t) {
        try {
            Class<?> clazz = field.getType();
            // No_5 集合
            if (List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())) {
                // 开启字段访问权
                field.setAccessible(true);
                // 字段原始类型
                Type type = field.getType();
                Class<?> originalClazz = Class.forName(type.getTypeName());
                // 获取集合容量
                Method sizeMethod = originalClazz.getDeclaredMethod("size");
                int size = (int) sizeMethod.invoke(field.get(t));
                // 遍历取值
                for (int i = 0; i < size; i++) {
                    // GetMethod
                    Method getMethod = originalClazz.getDeclaredMethod("get", int.class);
                    if (!getMethod.isAccessible()) {
                        getMethod.setAccessible(true);
                    }
                    Object itemValue = getMethod.invoke(field.get(t), i);
                    // log.info("[ " + field.getName() + " - Relation Field : " + String.valueOf(itemValue) + " ]");

                    // - Create XContentBuilder Field Object Start -
                    contentBuilder.startObject(String.valueOf(itemValue));

                    contentBuilder.field("type", "keyword");

                    contentBuilder.endObject();
                    // - Create XContentBuilder Field Object End -
                }
            } else {
                contentBuilder.startObject(field.getName());
                // No-1 字符串、字符串数组
                if (clazz.equals(String.class) || clazz.equals(String[].class)) {
                    boolean analyzed = (indexField == null) || indexField.analyzed();
                    // 默认为全文本类型（text），当配置analyzed is false时，即该字段不分词，字段类型为关键字类型（string）
                    // type配置
                    contentBuilder.field("type", analyzed ? "text" : "keyword");
                    // 索引配置
                    boolean index = (indexField != null) ? indexField.index() : true;
                    // 索引分词
                    if (index && analyzed) {
                        contentBuilder.field("analyzer", "ik_smart");
                    } else {
                        contentBuilder.field("index", "true");
                    }
                }
                // No-2 日期
                if (clazz.equals(Date.class) || clazz.equals(Date[].class)) {
                    // type配置
                    contentBuilder.field("type", "date");
                    // Date Format 日期格式化
                    contentBuilder.field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd");
                }
                // No-3 数字
                // 长整型（long Long）
                if (clazz.equals(Long.class) || clazz.equals(long.class) || clazz.equals(Long[].class) || clazz.equals(long[].class)) {
                    // type配置
                    contentBuilder.field("type", "long");
                }
                // 整型（int Integer）
                if (clazz.equals(Integer.class) || clazz.equals(int.class) || clazz.equals(Integer[].class) || clazz.equals(int[].class)) {
                    // type配置
                    contentBuilder.field("type", "integer");
                }
                // 短整型 （short Short）
                if (clazz.equals(Short.class) || clazz.equals(short.class) || clazz.equals(Short[].class) || clazz.equals(short[].class)) {
                    // type配置
                    contentBuilder.field("type", "short");
                }
                // 字节型（byte) // || clazz.equals(Byte[].class) || clazz.equals(byte[].class) // 字节数组排除
                if (clazz.equals(Byte.class) || clazz.equals(byte.class)) {
                    // type配置
                    contentBuilder.field("type", "byte");
                }
                // 单浮点型（float Float） - 32位精度浮点数
                if (clazz.equals(Float.class) || clazz.equals(float.class) || clazz.equals(Float[].class) || clazz.equals(float[].class)) {
                    // type配置
                    contentBuilder.field("type", "float");
                }
                // 双浮点型（double Double）- 64位精度浮点数
                if (clazz.equals(Double.class) || clazz.equals(double.class) || clazz.equals(Double[].class) || clazz.equals(double[].class)) {
                    // type配置
                    contentBuilder.field("type", "double");
                }
                // No-4 布尔
                if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
                    // type配置
                    contentBuilder.field("type", "boolean");
                }
                contentBuilder.endObject();
            }
        } catch (IOException | IllegalAccessException | NoSuchMethodException | InvocationTargetException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return contentBuilder;
    }

}
