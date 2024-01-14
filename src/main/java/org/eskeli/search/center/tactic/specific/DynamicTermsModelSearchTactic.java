package org.eskeli.search.center.tactic.specific;

import org.eskeli.search.center.tactic.BasicSearchTactic;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * Class Desc : 动态词汇建模搜索策略
 *
 * @author elisontl
 */
public class DynamicTermsModelSearchTactic extends BasicSearchTactic {

    @Override
    public QueryBuilder executeSearchTactic(QueryBuilder queryBuilder, String keywords, String highlightFieldNames, Class<?> idxClazz) {

        // 未完待续 ...
        // List<String> vocabularies = extendVocabularies(restHighLevelClient, searchParam.getKeywords());
        // QueryBuilders.multiMatchQuery(searchParam.getKeywords(), defaultHighlightFields);

        return null;
    }

    @Override
    public QueryBuilder executeSearchTactic(String keywords, String[] commonHighlightFields) {

        // 未完待续...
        return null;
    }

}
