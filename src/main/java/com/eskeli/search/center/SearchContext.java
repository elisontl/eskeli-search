package com.eskeli.search.center;

import com.eskeli.search.connection.ConnectClient;
import com.eskeli.search.center.engine.SearchEngine;
import com.eskeli.search.center.engine.GeneralSearchEngine;
import com.eskeli.search.entity.SearchFieldInformation;
import com.eskeli.search.entity.SearchParam;
import org.apache.http.util.Asserts;
import org.elasticsearch.client.RestHighLevelClient;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Class Desc : 搜索上下文
 *
 * @author elison.s
 */
public class SearchContext {

    // 搜索引擎对象
    private SearchEngine searchEngine;

    /**
     * Set注入SearchEngine对象
     * （提供此方法目的在于一些特殊搜索场景的中，对搜索策略的扩展）
     * （使得搜索工具除了通用性，同样具备扩展性）
     *
     * @param searchEngine : SearchEngine
     * @return
     */
    public void setSearchEngine(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    /**
     * 构造注入SearchEngine对象
     *
     * @param searchEngine : SearchEngine
     * @return
     */
    public SearchContext(SearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    /**
     * 构造函数（无参构造，默认方式，单节点连接模式）
     */
    public SearchContext() {
        if (searchEngine == null) {
            RestHighLevelClient restHighLevelClient = ConnectClient.SingleRestConnectClient.getSearchClient();
            searchEngine = new GeneralSearchEngine(restHighLevelClient);
        }
    }

    /**
     * 创建索引
     *
     * @param indexName : String : 索引名称
     * @param t : T : 索引实体类
     * @return
     */
    public <T> boolean createIndex(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return searchEngine.createIndex(indexName, t, searchFieldInformationMap);
    }

    /**
     * 批量发布数据（ 一 ）
     *
     * @param list ：List<T>
     * @return
     */
    public <T> boolean publishMultipleIndexData(List<T> list) {
        return publishMultipleIndexData(null, list, null);
    }

    /**
     * 批量发布数据（ 二 ）
     *
     * @param list : List<T>
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation
     * @param <T>
     * @return
     */
    public <T> boolean publishMultipleIndexData(List<T> list, Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return publishMultipleIndexData(null, list, searchFieldInformationMap);
    }

    /**
     * 批量发布数据 （ 三 ）
     *
     * @param indexName: String : 索引名称
     * @param list : List<T> : 索引数据
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation
     * @return
     */
    public <T> boolean publishMultipleIndexData(String indexName,
                                                List<T> list,
                                                Map<String, SearchFieldInformation> searchFieldInformationMap) {
        return searchEngine.publishMultipleIndexData(indexName, list, searchFieldInformationMap);
    }

    /**
     * 发布单条数据（ 一 ）
     *
     * @param t : T
     * @return
     */
    public <T> boolean publishIndexData(T t) {
        if (t instanceof Collection) {
            Asserts.check(t instanceof List<?>, "集合类型暂支持List，请指定集合类型为List");
            return publishMultipleIndexData((List<? extends Object>) t);
        }
        return publishIndexData(null, t, null);
    }

    /**
     * 发布单条数据（ 二 ）
     *
     * @param t : T
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation
     * @param <T>
     * @return
     */
    public <T> boolean publishIndexData(T t, Map<String, SearchFieldInformation> searchFieldInformations) {
        if (t instanceof Collection) {
            Asserts.check(t instanceof List<?>, "集合类型暂支持List，请指定集合类型为List");
            return publishMultipleIndexData(null, (List<?>) t, searchFieldInformations);
        }
        return publishIndexData(null, t, searchFieldInformations);
    }

    /**
     * 发布单条数据 ( 三 )
     *
     * @param indexName : String : 索引名称
     * @param t : T : 索引数据对象
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation
     * @return
     */
    public <T> boolean publishIndexData(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformations) {
        if (t instanceof Collection) {
            Asserts.check(t instanceof List<?>, "集合类型暂支持List，请指定集合类型为List");
            return searchEngine.publishMultipleIndexData(indexName, (List<?>) t, searchFieldInformations);
        }
        return searchEngine.publishIndexData(indexName, t, searchFieldInformations);
    }

    /**
     * 搜索方法 ( 精确搜索 )
     *
     * @param indexName : String : 索引名称
     * @param clazz : Class<R> : 索引类别
     * @param searchParam : SearchParam : 搜索参数Obj
     */

    public <R> List<R> search(String indexName, Class<R> clazz, SearchParam searchParam) {
        Asserts.check(searchParam != null, "构建 SearchContext Obj 时，请指定searchParam参数！");
        return searchEngine.search(indexName, clazz, searchParam);
    }

    @Deprecated
    public <R> List<R> search(String indexName, Class<R> clazz) {
        return searchEngine.search(indexName, clazz, new SearchParam());
    }

    /**
     * 统一搜索 ( 全局搜索 )
     *
     * @param searchParam : THSearchParam : 搜索参数
     * @return
     */
    public List<Map<String, Object>> executeGlobalSearch(SearchParam searchParam) {
        if (searchEngine instanceof GeneralSearchEngine) {
            return ((GeneralSearchEngine) searchEngine).globalSearch(ConnectClient.SingleRestConnectClient.getSearchClient(), searchParam);
        }
        return null;
    }

    /**
     * 删除索引（一）
     *
     * @param indexName : String : 索引名称
     * @return
     */
    public boolean deleteIndex(String indexName) {
        return searchEngine.deleteIndex(indexName);
    }

    /**
     * 删除索引（二）
     *
     * @param clazz : Class<T> : 索引数据类型
     * @return
     */
    public <T> boolean deleteIndex(Class<T> clazz) {
        return searchEngine.deleteIndex(clazz);
    }

}
