package org.eskeli.search.center.engine;

import org.eskeli.search.adapt.EntityFieldTypeAdapter;
import org.eskeli.search.annotation.KeliSearchIdxArea;
import org.eskeli.search.constant.KeliSearchConstant;
import org.eskeli.search.center.engine.idxunits.IndexFieldIdentifyComponent;
import org.eskeli.search.exprocess.CheckedConsumerProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class Desc : Bascal Achieve Search Engine
 *
 * @author elisontl
 */
@Slf4j
public abstract class BasicAchieveSearchEngine implements SearchEngine {

    /**
     * 数据类型适配
     *
     * Java Data Type 适配 ElasticSearch Data Type
     * @param contentBuilder : XContentBuilder : 内容构造器
     * @param clazz : Class<?> : 类型
     * @param indexField : IndexField : 字段索引注解对象（主要完成索引域的特性配置）
     * @return
     */
    public XContentBuilder adaptDataType(XContentBuilder contentBuilder, Class<?> clazz, KeliSearchIdxArea indexField) throws IOException {
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
                contentBuilder.field("analyzer","ik_smart");
            } else {
                contentBuilder.field("index", "true");
            }
        }
        // No-2 日期
        if (clazz.equals(Date.class) || clazz.equals(Date[].class)) {
            // type配置
            contentBuilder.field("type", "date");
            // 支持格式
            contentBuilder.field("format", "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis");
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
        return contentBuilder;
    }

    /**
     * Generate Search Result
     *
     * @param searchHits : SearchHit[]
     * @param highlightFieldNameArray : String[]
     * @return
     */
    public List<Map<String, Object>> generateSearchMapResult(SearchHit[] searchHits, String[] highlightFieldNameArray) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        Stream.of(searchHits).forEach(searchHit -> {
            // 普通记录结果
            Map<String, Object> recordMap = searchHit.getSourceAsMap();
            // 高亮记录结果
            Map<String, HighlightField> highlightFieldMap = searchHit.getHighlightFields();
            Set<String> recodeKeys = recordMap.keySet();
            if (recodeKeys != null) {
                recodeKeys.forEach(recodeKey -> {
                    // 如果当前字段为高亮字段，从高亮结果中取值，替换原值
                    if (ArrayUtils.contains(highlightFieldNameArray, recodeKey)) {
                        HighlightField highlightField =highlightFieldMap.get(recodeKey);
                        if (highlightField != null && highlightField.getFragments() != null && highlightField.getFragments().length > 0) {
                            recordMap.put(recodeKey, highlightFieldMap.get(recodeKey).getFragments()[0].string());
                        }
                    }
                });
            }
            resultList.add(recordMap);
        });
        return resultList;
    }

    /**
     * Generate Search Result
     *
     * @param searchHits : SearchHit[]
     * @param highlightFields : Field[]
     * @param clazz : Class<R>
     * @return
     */
    public <R> List<R> generateSearchResult(SearchHit[] searchHits, Field[] highlightFields, Class<R> clazz) {
        // 结果容器
        List<R> list = new ArrayList<>();
        Arrays.stream(searchHits).forEach(CheckedConsumerProcessor.accept(searchHit -> {
            // 构建返回值（R类型）对象
            R r_Obj = clazz.getDeclaredConstructor().newInstance();
            //  高亮索引域映射
            Map<String, HighlightField> highFieldMap = searchHit.getHighlightFields();
            // 全索引域映射
            Map<String, Object> allFieldMap = searchHit.getSourceAsMap();
            // 值注入
            r_Obj = achieveFieldValue(r_Obj, allFieldMap, highFieldMap, highlightFields);
            // 对象累加
            list.add(r_Obj);
        }));
        return list;
    }

    /**
     * 高亮域值注入
     *
     * @param r_Obj : R
     * @param allFieldMap : Map<String, Object>
     * @param highlightFieldMap : Map<String, HighlightField>
     * @param highlightFields : Field[]
     * @param <R>
     * @return
     */
    public <R> R achieveFieldValue(R r_Obj,
                                   Map<String, Object> allFieldMap,
                                   Map<String, HighlightField> highlightFieldMap,
                                   Field[] highlightFields) {

        Class clazz = r_Obj.getClass();

        // 对象转换过程: HighlightField Obj >> R Obj
        Stream.of(highlightFields).forEach(CheckedConsumerProcessor.accept(field -> {

            // 允许访问
            field.setAccessible(true);
            // 高亮字段值
            String highlightField_Val = null;

            // 非高亮字段值
            Object field_Val = null; // Class<?> fieldType = field.getType();

            // 高亮索引域，返回值为Array类型，非multi value索引域，通常取第一个值
            if (highlightFieldMap.containsKey(field.getName())) {
                HighlightField highlightField = highlightFieldMap.get(field.getName());
                Text[] texts = highlightField.getFragments();
                highlightField_Val = texts != null ? texts[0].toString() : "";
            }
            // 非高亮索引域
            else {
                Object docField = allFieldMap.get(field.getName());
                field_Val = docField;
            }

            // Set method generate .
            char[] filedChars = field.getName().toCharArray();
            filedChars[0] -= 32;
            String setMethodName = StringUtils.join(KeliSearchConstant.METHOD_NAME_PREFIX_SET, String.valueOf(filedChars));

            // 参数类型识别，参数传递
            Method setMethod = null;
            try {
                setMethod = clazz.getMethod(setMethodName, field.getType());
            } catch (NoSuchMethodException e) {
                // 异常反补
                setMethod = acquireSetMethodFailover(field, clazz, setMethod, setMethodName);
            }

            // 空值跳出
            if (field_Val == null) {
                return;
            }

            log.info("[ Invoke Field Name : " + field.getName() + " ]");

            Object proxyObj = (StringUtils.isNotEmpty(highlightField_Val))
                    ? setMethod.invoke(r_Obj, highlightField_Val) :
                    setMethod.invoke(r_Obj, EntityFieldTypeAdapter.adaptField_Val(field.getType(), field_Val));

            log.info(proxyObj != null ? String.valueOf(proxyObj.hashCode()) : "");

        }));

        return r_Obj;
    }

    /**
     * Method Desc : 异常反补( 容错 )
     *
     * @param field : Field
     * @param clazz : Class<T>
     * @param method : Method
     * @param methodName : String
     * @return
     */
    public <T> Method acquireSetMethodFailover(Field field,
                                               Class<T> clazz,
                                               Method method,
                                               String methodName) {

        /* 方法参数类型容错适配，解决包装类与原始类型互转问题 */
        Class<?> fieldTypeClazz = new EntityFieldTypeAdapter().mutualConvertFieldType(field);
        log.info("execute field type convert : {0} | after | {1}" , field.getType(), fieldTypeClazz);
        // setMethod()，存在性校验
        List<String> methodNames = Stream.of(clazz.getMethods())
                 .map(method0 -> method0.getName()).collect(Collectors.toList());
        try {
            if (methodNames.contains(methodName)) {
            method = clazz.getMethod(methodName, fieldTypeClazz);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return method;
    }

    /**
     * 转换索引源，字段类型适配
     
     * @param t : T : 任意类型对象
     * @return
     */
    public <T> Map<String, Object> convertSource2Map(T t) {
        Map<String, Object> sourceMap = new HashMap<>();
        Field[] fields = IndexFieldIdentifyComponent.synthesizeIndexFields(t.getClass());
        Stream.of(fields).forEach(CheckedConsumerProcessor.accept(field -> {
            field.setAccessible(true);
            KeliSearchIdxArea indexField = field.getAnnotation(KeliSearchIdxArea.class);
            String fieldName = (indexField != null && StringUtils.isNotEmpty(indexField.alias()) ? indexField.alias() : field.getName());
            // Date Field Dispose
            if (field.getType().isAssignableFrom(Date.class)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sourceMap.put(fieldName, simpleDateFormat.format(field.get(t)));
            } else {
                sourceMap.put(fieldName, field.get(t));
            }
        }));
        return sourceMap;
    }

}
