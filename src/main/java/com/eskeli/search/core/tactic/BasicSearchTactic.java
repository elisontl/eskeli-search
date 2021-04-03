package com.eskeli.search.core.tactic;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * Class Desc : Basal Search Tactics
 *
 * @author elison.s
 */
public abstract class BasicSearchTactic {

    /**
     *  Execute search tactic
     *
     * @param queryBuilder
     * @param keywords
     * @param highlightFieldNames
     * @param idxClazz
     * @return
     */
    public abstract QueryBuilder executeSearchTactic(QueryBuilder queryBuilder, String keywords, String highlightFieldNames, Class<?> idxClazz);

    /**
     *  Execute search tactic
     *
     * @param keywords
     * @param commonHighlightFields
     * @return
     */
    public abstract QueryBuilder executeSearchTactic(String keywords, String[] commonHighlightFields);

}

