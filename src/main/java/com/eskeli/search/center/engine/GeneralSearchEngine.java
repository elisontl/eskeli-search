package com.eskeli.search.center.engine;

import com.eskeli.search.entity.SearchFieldInformation;
import com.eskeli.search.entity.SearchParam;
import org.elasticsearch.client.RestHighLevelClient;
import java.util.List;
import java.util.Map;

/**
 * Class Desc : 基础搜索引擎
 *
 * @author elisontl
 */
public class GeneralSearchEngine extends UpperApplySearchEngine {

    private RestHighLevelClient restHighLevelClient;

    // Structure injection
    public GeneralSearchEngine(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    // Set method injection
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
     * 指定索引名（精确创建）
     *
     * @param indexName : String : 索引名称
     * @param t : T
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation> : 索引域配置信息
     * @param <T>
     * @return
     */
    @Override
    public <T> boolean createIndex(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return super.createIndex(restHighLevelClient, indexName, t, searchFieldInformationMap);
    }

    /**
     * 批量发布索引数据
     *
     * @param indexName : String : 索引名
     * @param list : List<T> : 索引的数据
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation> : 检索映射
     * @param <T>
     * @return
     */
    @Override
    public <T> boolean publishMultipleIndexData(String indexName, List<T> list, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return super.publishMultipleIndexData(restHighLevelClient, indexName, list, searchFieldInformationMap);
    }

    /**
     * 单条发布数据
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
