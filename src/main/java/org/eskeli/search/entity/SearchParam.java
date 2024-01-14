package org.eskeli.search.entity;

import org.eskeli.search.center.tactic.BasicSearchTactic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class Desc : 搜索参数
 *
 * @author elisontl
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchParam {

    // 搜索的字段（例如：title keywords content)，该字段可为空，为空时，由搜索中间件引擎自动识别待搜索的字段
    private String fieldName;

    // 搜索关键字
    private String keywords;

    // 分页参数对象
    private SearchResultPage page = new SearchResultPage();

    // 业务种类标识
    private String businessType;

    // 显式指定索引名
    private String indexName;

    // 数据类别id
    private String categoryId;

    // 数据类别
    private String category;

    // 搜索策略
    public BasicSearchTactic searchTactic;

    private String[] commonHighlightFields;

    /**
     * 搜索关键词，分页对象
     * @param keywords
     * @param page
     */
    public SearchParam(String keywords, SearchResultPage page) {
        this.keywords = keywords;
        this.page = page;
    }

}
