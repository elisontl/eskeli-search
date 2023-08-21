package com.eskeli.search.center.tactic.specific;

import com.eskeli.search.center.engine.idxunits.IndexFieldIdentifyComponent;
import com.eskeli.search.center.tactic.BasicSearchTactic;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Class Desc : Sample Search Tactic
 *
 * @author elison.s
 */
public class SampleSearchTactic extends BasicSearchTactic {

    /**
     *  Execute search tactic
     *
     * @param queryBuilder
     * @param keywords
     * @param highlightFieldNames
     * @param idxClazz
     * @return
     */
    @Override
    public QueryBuilder executeSearchTactic(QueryBuilder queryBuilder, String keywords, String highlightFieldNames, Class<?> idxClazz) {

        // Contain highlight scene
        if (StringUtils.isNotEmpty(keywords) && StringUtils.isNotEmpty(highlightFieldNames)){
            queryBuilder = QueryBuilders.multiMatchQuery(
                           keywords,
                           // Array format .
                           highlightFieldNames.split(",")
                           ).operator(Operator.OR);
        }
        // Not contain highlight scene
        else if (StringUtils.isNotEmpty(keywords) && StringUtils.isEmpty(highlightFieldNames)) {
            queryBuilder = QueryBuilders.multiMatchQuery(
                           keywords,
                           IndexFieldIdentifyComponent.synthesizeIndexFieldNameArray(idxClazz)
                           ).operator(Operator.OR);
        }
        // Matching all idx area
        else {
            queryBuilder = QueryBuilders.matchAllQuery();
        }

        return queryBuilder;
    }

    /**
     *  Execute search tactic
     *
     * @param keywords
     * @return
     */
    @Override
    public QueryBuilder executeSearchTactic(String keywords, String[] commonHighlightFields) {
        if (StringUtils.isEmpty(keywords)) {
            return QueryBuilders.matchAllQuery();
        }
        return QueryBuilders.multiMatchQuery(keywords, commonHighlightFields);
    }

}
