package com.eskeli.search.config;

import com.eskeli.search.annotation.KeliSearchIdxArea;
import com.eskeli.search.annotation.KeliSearchIdxEntity;
import com.eskeli.search.entity.SearchFieldInformation;
import com.eskeli.search.exprocess.CheckedConsumerProcessor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class Desc : Annotation Dynamic Config
 *
 * @author elisontl
 */
public class KeliSearchDynamicConfiguration {

    private static final String MEMBER_VALUES_KEY = "memberValues";

    /**
     * 实体对象字段注解的属性注入
     *
     * @param t : T : 实体 Obj
     * @param idxAreaAnnotationClazz : <U extends Annotation> : 注解类型
     * @param metadataTemplate : SearchFieldInformation : 数据检索模板 Obj
     * @return
     */
    public static <T, U extends Annotation> T injectFieldAnnotationProperties(
                    T t, Class<U> idxAreaAnnotationClazz, SearchFieldInformation metadataTemplate) {
        Arrays.stream(t.getClass().getDeclaredFields()).forEach(CheckedConsumerProcessor.accept(field -> {
            // 获取到指定注解的Obj
            U annotation_u = field.getAnnotation(idxAreaAnnotationClazz);
            if (annotation_u == null) {
                return;
            }
            InvocationHandler invocationHandle = Proxy.getInvocationHandler(annotation_u);
            Field mvField = invocationHandle.getClass().getDeclaredField(MEMBER_VALUES_KEY);
            mvField.setAccessible(true);
            Map<String, Object> mvMap = (Map<String, Object>) mvField.get(invocationHandle);
            Stream.of(metadataTemplate.getClass().getDeclaredFields())
                .forEach(CheckedConsumerProcessor.accept(
                    templateField -> {
                        if (templateField != null) {
                            templateField.setAccessible(true);
                            if (mvMap.containsKey(templateField.getName())) {
                                mvMap.put(templateField.getName(), templateField.get(metadataTemplate));
                            }
                        }
                }));
        }));
        return t;
    }

    /**
     * 动态配置实体注解
     * @param t : <T> : 任意类型实体
     * @param metadataFieldTemplateMap : Map<String, THSearchMetadataFieldTemplate> : 元据检索模板映射 Obj
     * @return
     */
    public static <T> T dynamicConfigKeliSearchAnnotation(T t, Map<String, SearchFieldInformation> metadataFieldTemplateMap) {
        /* 完成注解校验：校验当前实体哪些字段上配置有@KeliSearchIdxArea注解，
           只有通过该注解，可以动态传入加权、索引校验等域值 */
        Class clazz = t.getClass();
        Stream.of(clazz.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(KeliSearchIdxArea.class))
            .forEach(field -> {
                // --------------------------- Checked Code ---------------------------
                SearchFieldInformation metadataFieldTemplate = metadataFieldTemplateMap.get(field.getName());
                if (metadataFieldTemplate == null) {
                    return;
                }
                injectFieldAnnotationProperties(t, KeliSearchIdxEntity.class, metadataFieldTemplate);
        });
        return t;
    }

}
