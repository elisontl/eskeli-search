package org.eskeli.search.center.engine;

import org.eskeli.search.adapt.EntityTypeAdapter;
import org.eskeli.search.annotation.KeliSearchIdxArea;
import org.eskeli.search.annotation.KeliSearchNotIdxArea;
import org.eskeli.search.constant.KeliSearchConstant;
import org.eskeli.search.center.engine.idxunits.HighlightComponent;
import org.eskeli.search.center.engine.idxunits.IndexFieldIdentifyComponent;
import org.eskeli.search.center.func.EnrichFieldPropertiesFunction;
import org.eskeli.search.entity.SearchFieldInformation;
import org.eskeli.search.exprocess.CheckedConsumerProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

/**
 * Argument Config Search Engine
 *
 * @author elisontl
 */
@Slf4j
public abstract class ConfigStructureSearchEngine extends BasicAchieveSearchEngine {

    // HighlightBuilder obj
    private static final HighlightBuilder highlightBuilder = new HighlightBuilder();

    /**
     * 高亮设置方法 （ 一 ）
     * 精确指定字段，服务于精确搜索
     *
     * @param searchSourceBuilder : SearchSourceBuilder
     * @param clazz : Class<?>
     * @return
     */
    public HighlightComponent allocateHighlight(SearchSourceBuilder searchSourceBuilder, Class<?> clazz) {

        // - 高亮设置 Start -

        HighlightBuilder highlightBuilder = new HighlightBuilder();

        // 识别、设定高亮索引域
        Field[] fields = IndexFieldIdentifyComponent.synthesizeIndexFields(clazz);

        log.info("[ - 设置高亮索引域 Start - ]");

        StringBuffer highlightFieldNames = new StringBuffer("");

        if (fields != null) {
            Arrays.stream(fields).forEach(field -> {
                KeliSearchIdxArea idxArea = field.getDeclaredAnnotation(KeliSearchIdxArea.class);
                // 反向排除显式指定的非高亮域
                if (idxArea != null && (!idxArea.highlight())) {
                    return;
                }
                highlightBuilder.field(field.getName());
                highlightFieldNames.append(field.getName()).append(",");
            });
        }

        if (highlightFieldNames != null) {
            highlightFieldNames.deleteCharAt(highlightFieldNames.length() - 1);
        }

        log.info("[ - 设置高亮索引域 End - ]");

        // 高亮模式
        highlightBuilder.requireFieldMatch(Boolean.FALSE);
        // 高亮样式
        highlightBuilder.preTags(KeliSearchConstant.HIGHLIGHT_PREFIX).postTags(KeliSearchConstant.HIGHLIGHT_POSTFIX);
        // 最大高亮分片数 ( 枚举应用 )
        highlightBuilder.fragmentSize(1_0000);
        // 从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(0);

        // 并当高亮器 >> SearchSourceBuilder Obj
        searchSourceBuilder.highlighter(highlightBuilder);

        // - 高亮设置 End -

        return new HighlightComponent(fields, highlightFieldNames.toString(), searchSourceBuilder);
    }

    /**
     * 高亮设置方法 （ 二 ）
     *
     * @param searchSourceBuilder
     */
    public SearchSourceBuilder allocateGlobalSearchHighlight(SearchSourceBuilder searchSourceBuilder, String[] commonHighlightFields) {

        // 匹配模式
        highlightBuilder.requireFieldMatch(Boolean.FALSE);

        // 高亮样式
        highlightBuilder.preTags(KeliSearchConstant.HIGHLIGHT_PREFIX).postTags(KeliSearchConstant.HIGHLIGHT_POSTFIX);

        // 最大高亮分片数
        highlightBuilder.fragmentSize(1_0000);
        // 从第一个分片获取高亮片段
        highlightBuilder.numOfFragments(0);

        // 混排搜索（ 公共字段 ）
        if (commonHighlightFields != null) {
            Stream.of(commonHighlightFields).forEach(commonHighlightField -> {
                highlightBuilder.field(commonHighlightField);
            });
        }
        // 设置高亮器 （ 构建关联: SearchSourceBuilder Obj << HighlightBuilder Obj  ）
        searchSourceBuilder.highlighter(highlightBuilder);

        return searchSourceBuilder;
    }

    /**
     * 分页设置方法
     *
     * @param searchSourceBuilder : SearchSourceBuilder
     * @param start : int : 起始位置
     * @param size : int : 每页记录数
     */
    public void allocatePage(SearchSourceBuilder searchSourceBuilder, int start, int size) {
        Asserts.notNull(searchSourceBuilder, "SearchSourceBuilder 对象不能为空 ！");
        searchSourceBuilder.from(start).size(size);
    }

    /**
     * 创建映射（ Create Mapping ）
     * （依据实体字段类型，通过映射规则构建起Mapping's Fields）
     *
     * @param t : <T> : 任意类型实体
     * @param wholeFieldIndex : boolean : 是否全量索引
     * @return
     */
    public <T> XContentBuilder createIndexMapping(T t, boolean wholeFieldIndex,
                                                  Map<String, SearchFieldInformation> searchFieldInformationMap) throws IOException {

       Class<?> clazz = (t instanceof Collection) ? EntityTypeAdapter.resolveCollectionGenericClazz((Collection) t) : t.getClass();

        // 确定索引域
        Field[] fields = clazz.getDeclaredFields();
        // "mappings" - "type" - "properties"
        XContentBuilder mappingBuilder = XContentFactory.jsonBuilder();
        mappingBuilder.startObject();
        {
            mappingBuilder.startObject("properties");
            {
                // 字段索引场景分析 : 2.1 全字段索引 2.2 非全字段索引
                // 全字段索引（排除 @KeliSearchNotIndex 修饰的字段）
                if (wholeFieldIndex) {
                    Arrays.stream(fields)
                        .filter(field -> field.getDeclaredAnnotation(KeliSearchNotIdxArea.class) == null)
                        .forEach(CheckedConsumerProcessor.accept(field -> {
                            epipelagicIndexFieldProperties(field, mappingBuilder, searchFieldInformationMap);
                        }));
                }
                // 非全字段索引（只索引 @KeliSearchIdxArea 修饰字段）
                else {
                    Arrays.stream(fields)
                        .filter(field -> (field.getDeclaredAnnotation(KeliSearchIdxArea.class) != null))
                        .filter(field -> (field.getDeclaredAnnotation(KeliSearchNotIdxArea.class) == null))
                        .forEach(CheckedConsumerProcessor.accept(field -> {
                            epipelagicIndexFieldProperties(field, mappingBuilder, searchFieldInformationMap);
                        }));
                }
            }
            mappingBuilder.endObject();
        }
        mappingBuilder.endObject();

        return mappingBuilder;
    }

    /**
     * 构建索引映射（生于createIndexMapping方法，高于createIndexMapping方法）
     *
     * @param t
     * @param wholeFieldIndex
     * @param searchFieldInformationMap
     * @param enrichFieldPropertiesFunction : 动态指定策略
     * @param <T>
     * @return
     */
    public <T> XContentBuilder createIndexMapping(T t, boolean wholeFieldIndex,
                                                  Map<String, SearchFieldInformation> searchFieldInformationMap,
                                                  EnrichFieldPropertiesFunction enrichFieldPropertiesFunction) throws IOException {

        Class<?> clazz = (t instanceof Collection) ? EntityTypeAdapter.resolveCollectionGenericClazz((Collection) t) : t.getClass();

        // 确定索引域
        Field[] fields = clazz.getDeclaredFields();

        // "mappings" -- "type" -- "properties"
        XContentBuilder mappingBuilder = XContentFactory.jsonBuilder();

        mappingBuilder.startObject();
        {
            mappingBuilder.startObject("properties");
            {
                // 字段索引场景分析 : 2.1 全字段索引 2.2 非全字段索引
                // 全字段索引（排除 @KeliSearchNotIndex 修饰的字段）
                if (wholeFieldIndex) {
                    Arrays.stream(fields)
                        .filter(field -> field.getDeclaredAnnotation(KeliSearchNotIdxArea.class) == null)
                        .forEach(CheckedConsumerProcessor.accept(field -> {
                            enrichFieldPropertiesFunction.execute(
                                field,
                                mappingBuilder,
                                searchFieldInformationMap
                            );
                        }));
                }
                // 非全字段索引（只索引@KeliSearchIdxArea修饰字段）
                else {
                    Arrays.stream(fields)
                        .filter(field -> (field.getDeclaredAnnotation(KeliSearchIdxArea.class) != null))
                        .filter(field -> (field.getDeclaredAnnotation(KeliSearchNotIdxArea.class) == null))
                        .forEach(CheckedConsumerProcessor.accept(field -> {
                            enrichFieldPropertiesFunction.execute(
                                field,
                                mappingBuilder,
                                searchFieldInformationMap
                            );
                        }));
                }
            }
            mappingBuilder.endObject();
        }
        mappingBuilder.endObject();

        return mappingBuilder;
    }

    /**
     * 创建索引域（ Create Index Field ）
     *  （完成类型适配、属性配置等）
     *
     * @param field
     * @param contentBuilder
     * @param searchFieldInformationMap
     * @return
     */
    public XContentBuilder epipelagicIndexFieldProperties(Field field, XContentBuilder contentBuilder,
                                                          Map<String, SearchFieldInformation> searchFieldInformationMap) throws IOException, ClassNotFoundException {

        KeliSearchIdxArea indexField = field.getDeclaredAnnotation(KeliSearchIdxArea.class);

        Class<?> clazz = field.getType();

        String fieldName = (indexField != null && StringUtils.isNotEmpty(indexField.alias()) ? indexField.alias() : field.getName());

        // 添加索引域
        contentBuilder.startObject(fieldName);
        {
            /* 数据类型适配，总体分为三大类情况：No_1 : 基本数据类型 ( 包含Array ) /
                    No_2 : 集合类型 / No_3 : 复杂对象类型（Java对象类型、对象集合类型） */
            // 适配集合类型
            if (clazz.equals(List.class) || clazz.equals(Set.class)) {
                // Get到集合中的泛型类型，依据泛型类型，进行type设定（适配集合元素类型）
                ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                Type[] actualTypes = parameterizedType.getActualTypeArguments();
                if (actualTypes != null && actualTypes.length > 0) {
                    String actualTypeName = actualTypes[0].getTypeName();
                    contentBuilder = adaptDataType(contentBuilder, Class.forName(actualTypeName), indexField);
                }
            }
            // 适配基本数据类型
            else {
                contentBuilder = adaptDataType(contentBuilder, clazz, indexField);
            }

            // - 索引域属性配置（加权、存储等） start -
            SearchFieldInformation searchFieldInformation = null;

            if (searchFieldInformationMap != null) {
                searchFieldInformation = searchFieldInformationMap.get(field.getName());
            }

            if (searchFieldInformation != null) {
                float boost = (searchFieldInformation != null
                        ? searchFieldInformation.getBoost() : (indexField != null) ? indexField.boost() : 1.0f);
                log.info("[ " + field.getName() + " : " + boost + " ]");
            }

            // 加权
            contentBuilder.field("boost", searchFieldInformation != null
                    ? searchFieldInformation.getBoost() : (indexField != null) ? indexField.boost() : 1.0f);
            // 存储
            contentBuilder.field("store", searchFieldInformation != null
                    ? searchFieldInformation.isStore() : (indexField != null) ? indexField.store() : true);
            // - 索引域属性配置（加权、存储等） end -
        }
        contentBuilder.endObject();
        return contentBuilder;
    }

    /**
     * 查看 { 索引 } 的索引域
     *
     * @param restHighLevelClient : RestHighLevelClient
     * @param idxName : String : 索引名称
     * @return
     */
    public <T> LinkedHashMap<String, T> getIdxAreas(RestHighLevelClient restHighLevelClient, String idxName) {

        GetMappingsRequest getMappingsRequest = new GetMappingsRequest();

        getMappingsRequest.indices(idxName);

        GetMappingsResponse response = null;
        try {
            response = restHighLevelClient.indices().getMapping(getMappingsRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MappingMetadata mappingMetadata = response.mappings().get(idxName);

        Map<String, Object> sourceMap = mappingMetadata.getSourceAsMap();

        LinkedHashMap linkedHashMap = (LinkedHashMap) sourceMap.get("properties");

        return linkedHashMap;
    }

}
