package com.eskeli.search.center.engine;

import com.eskeli.search.annotation.KeliSearchIdxArea;
import com.eskeli.search.constant.KeliSearchConstant;
import com.eskeli.search.center.engine.idxunits.IndexFieldIdentifyComponent;
import com.eskeli.search.exprocess.CheckedConsumerProcessor;
import com.eskeli.search.factories.IdxMappingsFactory;
import com.eskeli.search.entity.SearchFieldInformation;
import com.eskeli.search.entity.SearchParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Class Desc : 高级搜索引擎
 *
 * @author elisontl
 */
@Slf4j
public class SmartSearchEngine extends UpperApplySearchEngine {

    private RestHighLevelClient restHighLevelClient;

    // Structure injection
    public SmartSearchEngine(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    // Set injection
    public void setRestHighLevelClient(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    /**
     * 创建索引（不指定索引名，自动生成索引名）
     *
     * @param t : T
     * @param <T>
     * @return
     */
    public <T> boolean createIndex(T t) {
        return super.createIndex(restHighLevelClient, t);
    }

    /**
     * 精准创建
     *
     * @param indexName : String : 索引名称
     * @param t : T
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation> : 索引域配置信息
     * @param <T>
     * @return
     */
    @Override
    public <T> boolean createIndex(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        XContentBuilder mappingBuilder = null;
        try {
            mappingBuilder = createIndexMapping(t, true, searchFieldInformationMap,
                    (field, contentBuilder, searchFieldInformationMap_1) -> IdxMappingsFactory.generateExtendIndexMappings
                    (contentBuilder, field, field.getDeclaredAnnotation(KeliSearchIdxArea.class), t));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return super.createIndexWithMapping(restHighLevelClient, indexName, t, mappingBuilder);
    }

    /**
     * 批量发布索引数据
     *
     * @param indexName  : String : 索引名
     * @param list : List<T> : 索引的数据
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation> : 检索映射
     * @return
     */
    @Override
    public <SmartIndex> boolean publishMultipleIndexData(String indexName, List<SmartIndex> list, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return super.publishMultipleIndexData(restHighLevelClient, indexName, list, searchFieldInformationMap, extendIndex -> smartConvertSourceToMap(extendIndex));
    }

    /**
     * 资源转换函数
     *
     * @param smartIndex
     * @param <SmartIndex>
     * @return
     */
    public <SmartIndex> Map<String, Object> smartConvertSourceToMap(SmartIndex smartIndex) {
        Map<String, Object> sourceMap = new HashMap<>();
        Field[] fields = IndexFieldIdentifyComponent.synthesizeIndexFields(smartIndex.getClass());
        Stream.of(fields).forEach(CheckedConsumerProcessor.accept(field -> {
            field.setAccessible(true);
            if (List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                Type type = field.getType();
                // OriginalClazz
                Class<?> originalClazz = Class.forName(type.getTypeName());
                Method sizeMethod = originalClazz.getDeclaredMethod(KeliSearchConstant.PROPERTY_NAME_PREFIX_SIZE);
                int size = (int) sizeMethod.invoke(field.get(smartIndex));
                for (int i = 0; i < size; i++) {
                    Method getMethod = originalClazz.getDeclaredMethod(KeliSearchConstant.METHOD_NAME_PREFIX_GET, int.class);
                    if (!getMethod.isAccessible())
                        getMethod.setAccessible(true);
                    // ----------------- 解析处理逻辑 --------------------------
                }
            } else {
                sourceMap.put(field.getName(), field.get(smartIndex));
            }
        }));
        return sourceMap;
    }

    /**
     * 单条发布索引数据
     *
     * @param indexName : String : 索引名（如果不指定索引名，按当前t类型自动合成，确定索引名）
     * @param t : T
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation>
     * @param <T>
     * @return
     */
    @Override
    public <T> boolean publishIndexData(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return super.publishIndexData(restHighLevelClient, indexName, t, searchFieldInformationMap);
    }

    /**
     * 搜索方法
     *
     * @param indexName : String : 索引名称
     * @param clazz : Class<R> : 该参数，作用有二 (1) 指定返回类型 (2) 当未指定索引名indexName时，依据此类型，可自动识别索引
     * @param searchParam : SearchParam : 搜索参数对象
     * @param <R>
     * @return
     */
    @Override
    public <R> List<R> search(String indexName, Class<R> clazz, SearchParam searchParam) {
        return super.search(restHighLevelClient, indexName, clazz, searchParam);
    }

    /**
     * 删除索引（按索引名称删除）
     *
     * @param indexName : String : 索引名称
     * @return
     */
    @Override
    public boolean deleteIndex(String indexName) {
        return super.deleteIndex(restHighLevelClient, indexName);
    }

    /**
     * 删除索引（按类型删除）
     *
     * @param clazz : Class<T> : 索引数据类型
     * @param <T>
     * @return
     */
    @Override
    public <T> boolean deleteIndex(Class<T> clazz) {
        return super.deleteIndex(restHighLevelClient, clazz);
    }

}
