package org.eskeli.search.center.engine;

import org.eskeli.search.entity.SearchFieldInformation;
import org.eskeli.search.entity.SearchParam;

import java.util.List;
import java.util.Map;

/**
 * Class Desc : 基础索引、搜索方法
 * （ 各检索实现端可在此基础上进行实现扩展 ）
 *
 * @author elisontl
 */
public interface SearchEngine {

    /**
     * 创建索引（默认方式，不显式声明索引名称，将所传入对象类型名作为索引名）
     *
     * @param t : T
     * @return
     */
    <T> boolean createIndex(T t);

    /**
     * 创建索引（方法重载，显式声明索引名称）
     *
     * @param indexName : String : 索引名称
     * @param t : T : 任意类型实体对象
     * @param searchFieldInformationMap : Map<String, THSearchFieldInformation> : 索引域配置信息
     * @return
     */
    <T> boolean createIndex(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap);

    /**
     * 添加索引数据（批量）
     *
     * @param indexName                 : String : 索引名
     * @param list                      : List<T> : 索引的数据
     * @param searchFieldInformationMap : Map<String, THSearchFieldInformation> : 检索映射
     * @return
     */
    <T> boolean publishMultipleIndexData(String indexName, List<T> list, Map<String, SearchFieldInformation> searchFieldInformationMap);

    /**
     * 添加索引数据
     *
     * @param indexName : String : 索引名（如果不指定索引名，按当前t类型自动合成，确定索引名）
     * @param t : T
     * @param searchFieldInformationMap : Map<String, THSearchFieldInformation>
     * @return
     */
    <T> boolean publishIndexData(String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap);

    /**
     * 搜索数据 ( 重构Api )
     *
     * @param indexName   : String : 索引名称
     * @param clazz       : Class<R> : 该参数，作用有二 (1) 指定返回类型 (2) 当未指定索引名indexName时，依据此类型，可自动识别索引
     * @param searchParam : SearchParam : 搜索参数对象
     */
    <R> List<R> search(String indexName, Class<R> clazz, SearchParam searchParam);

    /**
     * 删除索引（ 方式一 ）
     *
     * @param indexName : String : 索引名称
     * @return
     */
    boolean deleteIndex(String indexName);

    /**
     * 删除索引（ 方式二 ）
     *
     * @param clazz : Class<T> : 索引数据类型
     * @return
     */
    <T> boolean deleteIndex(Class<T> clazz);

}
