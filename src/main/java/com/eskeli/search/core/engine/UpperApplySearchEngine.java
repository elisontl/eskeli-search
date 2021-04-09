package com.eskeli.search.core.engine;

import com.eskeli.search.adapt.EntityTypeAdapter;
import com.eskeli.search.annotation.KeliSearchIdxEntity;
import com.eskeli.search.constant.KeliSearchConstant;
import com.eskeli.search.core.engine.idxunits.HighlightComponent;
import com.eskeli.search.core.func.ConvertSourceToMapFunction;
import com.eskeli.search.core.tactic.SearchTacticInvoker;
import com.eskeli.search.factories.IdxComponentFactory;
import com.eskeli.search.factories.IdxMetaSettingsFactory;
import com.eskeli.search.entity.IdxComponent;
import com.eskeli.search.entity.SearchFieldInformation;
import com.eskeli.search.entity.SearchParam;
import com.eskeli.search.utils.AnalyzeTermsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class Desc : 上层搜索引擎
 *
 * @author elison.s
 */
@Slf4j
public abstract class UpperApplySearchEngine extends ConfigStructureSearchEngine {

    // Search tactics invoker property
    private SearchTacticInvoker searchTacticInvoker;

    public UpperApplySearchEngine() {
        this.searchTacticInvoker = new SearchTacticInvoker();
    }

    /**
     * 创建索引（索引名自动生成）
     *
     * @param t : T
     * @param <T>
     * @return
     */
    public <T> boolean createIndex(RestHighLevelClient highLevelClient, T t) {
        return createIndex(highLevelClient, null, t, null);
    }

    /**
     * 创建索引
     * （任何数据类型实体，均可完成数据索引，索引程序只写一次）
     *
     * @param indexName : String : 索引名
     * @param t : T : 任意类型实体
     * @param searchFieldInformationMap : Map<String, SearchFieldInformation> : 索引域配置信息集合
     * @return
     */
    public <T> boolean createIndex(RestHighLevelClient restHighLevelClient, String indexName, T t, Map<String, SearchFieldInformation> searchFieldInformationMap) {

        // 参数非空校验
        if (t == null) return false;

        // Get实体对象t的Class对象，适配无限定类型实体
        Class<?> clazz = (t instanceof Collection) ? EntityTypeAdapter.resolveCollectionGenericClazz((Collection) t) : t.getClass();

        // 索引元件对象
        IdxComponent indexComponent = IdxComponentFactory.generateIndexName(clazz);

        // t的索引注解@KeliSearchIdxEntity对象
        KeliSearchIdxEntity indexClazz = indexComponent.getIndexClazzObject();

        // 1) 确定索引名
        indexName = (StringUtils.isNotEmpty(indexName)) ? indexName : indexComponent.getIndexName();

        // 2) 索引覆盖检查: 获取名为indexName的索引，校验该索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (exists) {
                // 索引已存在时，分两种情况，执行索引覆盖or抛出运行时异常，告知使用者
                // 索引覆盖
                if (indexClazz != null && indexClazz.indexCover()) {
                    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
                    AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
                    if (acknowledgedResponse.isAcknowledged()) {
                        log.info(MessageFormat.format("旧索引{0}已删除，新索引待创建", indexName));
                    }
                    // 索引不覆盖
                } else {
                    // throw new RuntimeException("索引{ " + indexName + " }已存在");
                    log.info(MessageFormat.format("索引{0}，已存在，无重建", indexName));
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

        // 3) - 索引设置 Start -
        Map<String, String> paramMap = new HashMap<>();
        // 设置分片
        if (indexClazz != null && indexClazz.shardsNumber() != 0) {
            paramMap.put("number_of_shards", String.valueOf(indexClazz.shardsNumber()));
        }
        // 设置副本
        if (indexClazz != null && indexClazz.replicasNumber() != 0) {
            paramMap.put("number_of_replicas", String.valueOf(indexClazz.replicasNumber()));
        }

        Settings settings = paramMap.isEmpty() ? IdxMetaSettingsFactory.generateIndexSettings() : IdxMetaSettingsFactory.generateIndexSettings(paramMap);
        createIndexRequest.settings(settings);
        // - 索引设置 End -

        // 4）索引映射（映射、类型）
        // -------- Create Index Mapping Start -------
        try {
            // Create Index Request Mapping
            XContentBuilder mappingBuilder = createIndexMapping(t, (indexClazz != null ? indexClazz.wholeAreaIndex() : true), searchFieldInformationMap);
            // 映射类型type
            createIndexRequest.mapping(indexClazz != null && StringUtils.isNotEmpty(indexClazz.indexType()) ? indexClazz.indexType() : "default", mappingBuilder);

            // 5) 创建索引（indexName）
            // Execute Index Create
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (createIndexResponse.isAcknowledged()) {
                log.info(MessageFormat.format("索引{0}创建成功", indexName));
                return true;
            }
        } catch (Exception e) {
            log.error(MessageFormat.format("索引{0}创建异常", indexName));
            e.printStackTrace();
        }
        // -------- Create Index Mapping End -------

        return false;
    }

    /**
     * 创建索引（指定mapping方式）
     *
     * @param restHighLevelClient : RestHighLevelClient : 搜索引擎链接客户端
     * @param indexName : String : 索引名称
     * @param mappingBuilder : XContentBuilder : 指定的Mapping映射
     * @return
     */
    public <T> boolean createIndexWithMapping(RestHighLevelClient restHighLevelClient, String indexName, T t, XContentBuilder mappingBuilder) {

        // 参数非空校验
        if (t == null) return false;

        // Get实体对象t的Class对象，适配无限定类型实体
        Class<?> clazz = (t instanceof Collection) ? EntityTypeAdapter.resolveCollectionGenericClazz((Collection) t) : t.getClass();

        // 索引元件对象
        IdxComponent indexComponent = IdxComponentFactory.generateIndexName(clazz);

        // t的索引注解@KeliSearchIdxEntity对象
        KeliSearchIdxEntity indexClazz = indexComponent.getIndexClazzObject();

        // 1) 确定索引名
        indexName = (StringUtils.isNotEmpty(indexName)) ? indexName : indexComponent.getIndexName();

        // 2) 索引覆盖检查: 获取名为indexName的索引，校验该索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        try {
            boolean exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
            if (exists) {
                // 索引已存在时，分两种情况，执行索引覆盖or抛出运行时异常，告知使用者
                // 索引覆盖
                if (indexClazz != null && indexClazz.indexCover()) {
                    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
                    AcknowledgedResponse acknowledgedResponse = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
                    if (acknowledgedResponse.isAcknowledged()) {
                        log.info(MessageFormat.format("索引{0}，已删除", indexName));
                    }
                    // 索引不覆盖
                } else {
                    // throw new RuntimeException("索引{ " + indexName + " }已存在");
                    log.info(MessageFormat.format("索引{0}，已存在", indexName));
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);

        // 3) - 索引设置 Start -
        Map<String, String> paramMap = new HashMap<>();
        if (indexClazz != null && indexClazz.shardsNumber() != 0) {
            paramMap.put("number_of_shards", String.valueOf(indexClazz.shardsNumber()));
        }
        if (indexClazz != null && indexClazz.replicasNumber() != 0) {
            paramMap.put("number_of_replicas", String.valueOf(indexClazz.replicasNumber()));
        }

        Settings settings = paramMap.isEmpty() ? IdxMetaSettingsFactory.generateIndexSettings() : IdxMetaSettingsFactory.generateIndexSettings(paramMap);
        createIndexRequest.settings(settings);
        // - 索引设置 End -

        // 4）索引映射（映射、类型）
        // -------- Create Index Mapping Start -------
        try {
            // 映射类型type
            createIndexRequest.mapping(indexClazz != null && StringUtils.isNotEmpty(indexClazz.indexType()) ? indexClazz.indexType() : "default", mappingBuilder);

            // 5) 创建索引（indexName）
            // Execute Index Create
            CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
            if (createIndexResponse.isAcknowledged()) {
                log.info(MessageFormat.format("-- 索引{0}构建完成 --", indexName));
                return true;
            }
        } catch (Exception e) {
            log.error(MessageFormat.format("索引{0}创建异常", indexName));
            e.printStackTrace();
        }
        // -------- Create Index Mapping End -------

        return false;
    }

    /**
     * 批量发布索引数据
     *
     * @param indexName : String : 索引名称
     * @param list : List<T> : 数据列表
     * @return
     */
    public <T> boolean publishMultipleIndexData(RestHighLevelClient restHighLevelClient,
                                                String indexName, List<T> list,
                                                Map<String, SearchFieldInformation> searchFieldInformationMap) {
        // 参数合法性校验
        if (list == null || list.isEmpty()) {
            return false;
        }

        // 索引名
        indexName = (StringUtils.isEmpty(indexName) && (!list.isEmpty()))
                ? IdxComponentFactory.generateIndexName(list.get(0).getClass()).getIndexName() : indexName;

        // 索引检查
        boolean checkIndexExist = createIndex(restHighLevelClient, indexName, list.get(0), searchFieldInformationMap);
        if (checkIndexExist) {
            log.info("publishMultipleIndexData(),索引检查完成，已准备就绪，start发布数据！");
        }

        // 批量索引构建请求
        BulkRequest bulkRequest = new BulkRequest(indexName);
        list.forEach(t -> {
            IndexRequest indexRequest = new IndexRequest();
            Map<String, ?> sourceMap = convertSource2Map(t);
            log.info("[ sourceMap : " + sourceMap + " ]");
            /* 使用es中_doc，默认的_id属性 （也可使用t自身id属性）
               indexRequest.id(sourceMap.get("id")); */
            indexRequest.source(sourceMap, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (!bulkResponse.hasFailures()) {
                return true;
            } else {
                log.info( bulkResponse.buildFailureMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量发布索引数据（支持动态指定字段映射策略）
     *
     * @param restHighLevelClient
     * @param indexName
     * @param list
     * @param searchFieldInformationMap
     * @param convertSource2MapFunction
     * @param <T>
     * @return
     */
    public <T> boolean publishMultipleIndexData(RestHighLevelClient restHighLevelClient,
                                                String indexName,
                                                List<T> list,
                                                Map<String, SearchFieldInformation> searchFieldInformationMap,
                                                ConvertSourceToMapFunction convertSource2MapFunction) {
        // 参数合法性校验
        if (list == null || list.isEmpty()) {
            return false;
        }

        // 索引名
        indexName = (StringUtils.isEmpty(indexName) && (!list.isEmpty()))
                ? IdxComponentFactory.generateIndexName(list.get(0).getClass()).getIndexName() : indexName;

        // 索引检查
        boolean checkIndexExist = createIndex(restHighLevelClient, indexName, list.get(0), searchFieldInformationMap);
        if (checkIndexExist) {
            log.info(" [ publishMultipleIndexData(),索引检查完成，已准备就绪，start发布数据！]");
        }

        // 批量索引构建请求
        BulkRequest bulkRequest = new BulkRequest(indexName);
        list.forEach(t -> {
            IndexRequest indexRequest = new IndexRequest();
            Map<String, ?> sourceMap = convertSource2MapFunction.convert(t);
            // log.info("[ sourceMap : " + sourceMap + " ]");
            indexRequest.source(sourceMap, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        try {
            BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (!bulkResponse.hasFailures()) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 添加索引数据（单条添加）
     *
     * @param indexName : String : 索引名
     * @param t : T
     * @param <T>
     * @return
     */
    public <T> boolean publishIndexData(RestHighLevelClient restHighLevelClient,
                                        String indexName,
                                        T t,
                                        Map<String, SearchFieldInformation> searchFieldInformationMap) {


        Asserts.notNull(t, "参数不合法，接收到 T 类型对象 t 为空 !");

        // 索引名
        indexName = (StringUtils.isEmpty(indexName))
                ? IdxComponentFactory.generateIndexName(
                    (t instanceof Collection) ? EntityTypeAdapter.resolveCollectionGenericClazz((Collection) t) : t.getClass()
                ).getIndexName() : indexName;

        // 索引检查
        boolean checkIndexExist = createIndex(restHighLevelClient, indexName, t, searchFieldInformationMap);
        if (checkIndexExist) {
            log.info("publishMultipleIndexData(),索引检查完成，已准备就绪，start发布数据！");
        }

        IndexRequest indexRequest = new IndexRequest(indexName);
        Map<String, ?> sourceMap = convertSource2Map(t);
        /* 使用es中_doc，默认的_id属性 （也可使用t自身id属性）
           indexRequest.id(sourceMap.get("id")); */
        indexRequest.source(sourceMap, XContentType.JSON);

        try {
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if (indexResponse.status() == RestStatus.CREATED) {
                log.info("索引数据发布成功");
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取索引库中，所有索引（名称）
     *
     * @param restHighLevelClient : RestHighLevelClient
     * @return
     */
    public String[] getIndices(RestHighLevelClient restHighLevelClient) {
        try {
            Request request = new Request("GET", "/_cat/indices?h=i");
            RestClient restClient = restHighLevelClient.getLowLevelClient();
            InputStream is = restClient
                                .performRequest(request)
                                .getEntity()
                                .getContent();
            return new BufferedReader(new InputStreamReader(is))
                                .lines()
                                .collect(Collectors.toList())
                                .toArray(new String[] {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * 获取索引库中所有业务索引（名）
     *
     * @param
     * @return
     */
    public String[] getBusinessIndices(RestHighLevelClient restHighLevelClient) {
        // 获取业务索引
        String[] businessIndices = getIndices(restHighLevelClient);

        businessIndices = Arrays.stream(businessIndices)
                // 业务数据索引 / 文本段落索引
                .filter(businessIndex -> businessIndex.startsWith(
                        KeliSearchConstant.DATA_INDEX_PREFIX) || businessIndex.startsWith("text_"))
                .collect(Collectors.toList())
                .toArray(new String[] {});
        return businessIndices;
    }

    /**
     * 精准搜索（ 依据业务数据类别搜索 ）
     *
     * @param indexName : String : 索引名称
     * @param clazz : Class<R> : 该参数作用有二，
    *         (1)指定返回类型 (2)当未指定索引名indexName时，依据此类型，可自动识别索引
     * @param searchParam : SearchParam : 搜索参数对象
     * @return
     */
    public <R> List<R> search(RestHighLevelClient restHighLevelClient,
                               String indexName, Class<R> clazz, SearchParam searchParam) {

        // 类型不能为空，该搜索方式为业务数据精确搜索方式 : ( 业务类别 )
        Objects.requireNonNull(clazz, "执行search()方法时，clazz参数不可为空！");

        // 获取索引（名）
        indexName = (StringUtils.isNotEmpty(indexName)) ? indexName : (
                (clazz != null) ? IdxComponentFactory.generateIndexName(clazz).getIndexName() : null
        );

        // Create SearchRequest obj
        SearchRequest searchRequest;
        if (StringUtils.isNotEmpty(indexName)) {
            searchRequest = new SearchRequest(indexName);
        } else {
            String[] businessIndices = getBusinessIndices(restHighLevelClient);
            searchRequest = new SearchRequest(businessIndices);
        }

        // Create SearchSourceBuilder obj
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // Allocate highlight
        HighlightComponent highlightComponent = allocateHighlight(searchSourceBuilder, clazz);

        // Allocate page
        allocatePage(searchSourceBuilder,
                    (Objects.isNull(searchParam)) ?  0 : searchParam.getPage().getStart(),
                    (Objects.isNull(searchParam)) ? 10 : searchParam.getPage().getPageSize());

        // Allocate search tactic
        QueryBuilder queryBuilder = null;

        queryBuilder = searchTacticInvoker.searchTactic.executeSearchTactic(
            queryBuilder, searchParam.getKeywords(), highlightComponent.getHighlightFieldNames(), clazz
        );

        // Associated QueryBuilder obj >> SearchSourceBuilder obj >> SearchRequest obj construction
        searchRequest.source(
            searchSourceBuilder.query(queryBuilder)
        );

        try {
            // Execute search .
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // Achieve search result data .
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            // Adapt result, injection data value .
            return generateSearchResult(searchHits, highlightComponent.getHighlightFields(), clazz);

        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("[ 搜索异常，返回空值 ]");

        return null;
    }

    /**
     * 统一搜索（ 数据混排 ）
     * 统一搜索 >> 结果列表（List<Map>）>> 遍历列表 >> 类型判断 >> 依据类型显示数据字段
     *
     * @param restHighLevelClient : RestHighLevelClient
     * @param searchParam : SearchParam : 搜索参数
     * @return
     */
    public List<Map<String, Object>> globalSearch(RestHighLevelClient restHighLevelClient, SearchParam searchParam) {

        Objects.requireNonNull(searchParam, "请指定searchParam参数，该参数不能为空 ！");

        // 获取参与全局统一搜索的 { 索引 }
        String[] businessIndices = getBusinessIndices(restHighLevelClient);

        SearchRequest searchRequest = new SearchRequest(businessIndices);

        // SearchSourceBuilder Obj
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // Common highlight fields
        String[] commonHighlightFields = Objects.isNull(searchParam)
                ? new String[] { "title", "content" } : searchParam.getCommonHighlightFields();

        // Allocate global search fields
        allocateGlobalSearchHighlight(searchSourceBuilder, commonHighlightFields);

        // Allocate page
        allocatePage(searchSourceBuilder,
                (Objects.isNull(searchParam)) ?  0 : searchParam.getPage().getStart(),
                (Objects.isNull(searchParam)) ? 10 : searchParam.getPage().getPageSize());

        // 动态指定策略
        if (!Objects.isNull(searchParam) && !Objects.isNull(searchParam.searchTactic)) {
            searchTacticInvoker = new SearchTacticInvoker(searchParam.searchTactic);
        }

        // 这里的 fields，指定为所有数据的共有字段，执行全局统一搜索
        QueryBuilder queryBuilder =
                searchTacticInvoker.searchTactic.executeSearchTactic(searchParam.getKeywords(), commonHighlightFields);

        // Associated QueryBuilder obj >> SearchSourceBuilder obj >> SearchRequest obj construction
        searchRequest.source(
                searchSourceBuilder.query(queryBuilder)
        );

        try {
            // Execute search .
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // Achieve search result data .
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            // Adapt result, injection data value .
            return generateSearchMapResult(searchHits, commonHighlightFields);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Extend Vocabularies
     *
     * @param restHighLevelClient : RestHighLevelClient : 高级别 Client
     * @param keywords : String : 搜索关键字
     * @return
     */
    public List<String> extendVocabularies(RestHighLevelClient restHighLevelClient, String keywords) {
        return AnalyzeTermsUtils.processAnalyzeTerms(restHighLevelClient, keywords);
    }

    /**
     * 删除索引（方式一：指定索引名称，删除索引）
     *
     * @param indexName : String : 索引名称
     * @return
     */
    public boolean deleteIndex(RestHighLevelClient restHighLevelClient, String indexName) {
        // 结果变量
        boolean result = false;

        try {
            // Create DeleteIndexRequest Obj
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            // 删除索引
            AcknowledgedResponse response = restHighLevelClient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);

            result = response.isAcknowledged();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 删除索引（方式二：指定数据类型，自动生成索引名称，删除索引）
     *
     * @param clazz : Class<T> : 索引数据类型
     * @return
     */
    public <T> boolean deleteIndex(RestHighLevelClient restHighLevelClient, Class<T> clazz) {
        // 结果变量
        boolean result = false;
        // 依据指定类型，生成索引组件
        IdxComponent indexComponent = IdxComponentFactory.generateIndexName(clazz);
        // 从索引组件中获取索引名称
        String indexName = indexComponent.getIndexName();
        // 删除索引
        return deleteIndex(restHighLevelClient, indexName);
    }

}
