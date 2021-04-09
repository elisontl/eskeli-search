package com.eskeli.search.adapt;

import com.eskeli.search.utils.DataFormatUtils;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class Desc : 数据域（实体字段、索引域）类型适配器
 *
 * @author elison.s
 */
public class EntityFieldTypeAdapter {

    /**
     * Java对象字段包装类型与原始类型之间互相转换
     *
     * @param field : Field
     * @return
     */
    public Class<?> mutualConvertFieldType(Field field) {
        Class<?> fieldTypeClazz = field.getType();
        // 字节
        if (fieldTypeClazz == Byte.class || fieldTypeClazz == byte.class) {
            fieldTypeClazz = fieldTypeClazz == Byte.class ? byte.class : Byte.class;
        }
        // 32位整型
        if (fieldTypeClazz == Integer.class || fieldTypeClazz == int.class) {
            fieldTypeClazz = fieldTypeClazz == Integer.class ? int.class : Integer.class;
        }
        // 短整型
        if (fieldTypeClazz == Short.class || fieldTypeClazz == short.class) {
            fieldTypeClazz = fieldTypeClazz == Short.class ? short.class : Short.class;
        }
        // 长整型
        if (fieldTypeClazz == Long.class || fieldTypeClazz == long.class) {
            fieldTypeClazz = fieldTypeClazz == Long.class ? long.class : Long.class;
        }
        // 32位浮点数
        if (fieldTypeClazz == Float.class || fieldTypeClazz == float.class) {
            fieldTypeClazz = fieldTypeClazz == Float.class ? float.class : Float.class;
        }
        // 64位浮点数
        if (fieldTypeClazz == Double.class || fieldTypeClazz == double.class) {
            fieldTypeClazz = fieldTypeClazz== Float.class ? float.class : Float.class;
        }
        // 字符
        if (fieldTypeClazz == Character.class || fieldTypeClazz == char.class) {
            fieldTypeClazz = fieldTypeClazz== Character.class ? char.class : Character.class;
        }
        // 布尔
        if (fieldTypeClazz == Boolean.class || fieldTypeClazz == boolean.class) {
            fieldTypeClazz = fieldTypeClazz== Boolean.class ? boolean.class : Boolean.class;
        }
        return fieldTypeClazz;
    }

    /**
     * 转换值类型
     *
     * @param clazz : Class<T>
     * @param field_Val : Object
     */
    public static <T> T adaptField_Val(Class<T> clazz, Object field_Val) throws ParseException {
        // Date 类型
        if (clazz.isAssignableFrom(Date.class)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = format.parse(String.valueOf(field_Val));
            return (T) date;
        }
        // Long 类型
        else if (clazz.isAssignableFrom(Long.class) || clazz.isAssignableFrom(long.class)) {
            return (T) Long.getLong(field_Val + "L");
        }
        // Float 类型
        else if (clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(float.class)) {
            return (T) Float.valueOf(String.valueOf(field_Val));
        }
        // Double 类型
        else if (clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(double.class)) {
            return (T) Double.valueOf(String.valueOf(field_Val));
        }
        else {
            return (T) field_Val;
        }
    }

    /**
     * Get字段原始类型值
     *
     * @param field : Field : 字段对象
     * @param fieldValue : Object : 字段值
     * @param checkClazz : Class<T> : 类型
     * @return
     */
    public static <T> T getFieldOriginalType(Field field, Object fieldValue, Class<T> checkClazz) {
        Class<?> clazz = field.getType();
        return clazz.isAssignableFrom(checkClazz) ? (T) fieldValue : null;
    }

    /**
     * 特殊字段处理，“日期” - 转换成 - 字符串
     *
     * @param field : Field : 字段对象
     * @param t : T : 任意类型对象
     * @return
     */
    public static <T> String processEspecialField(Field field, T t) throws IllegalAccessException {
        if (field.getType().isAssignableFrom(Date.class)) {
            Date value = (Date) field.get(t);
            // yyyy-MM-dd HH:mm:ss (年月日时分秒，全格式）
            return DataFormatUtils.parseDate2FullCharacters(value);
        }
        return String.valueOf(field.get(t));
    }

}
