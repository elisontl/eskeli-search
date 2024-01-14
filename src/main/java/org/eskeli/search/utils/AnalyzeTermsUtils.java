package org.eskeli.search.utils;

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class Desc : Analyze Utils
 *
 * @author elisontl
 */
public class AnalyzeTermsUtils {

    /**
     * 获取指定字符串分词
     *
     * @param restHighLevelClient
     * @param keywords
     * @return
     */
    public static List<String> processAnalyzeTerms(RestHighLevelClient restHighLevelClient, String keywords) {
        List<String> analyzeTerms = new ArrayList<>();
        Map<String, Object> stopFilter = new HashMap<>();
        stopFilter.put("type", "stop");
        // 您有停用词 ？ 使劲往里加 ！ 回见了您
        stopFilter.put("stopwords", new String[]{ "to", "啊", "的", "吧" });
        AnalyzeRequest analyzeRequest = AnalyzeRequest.buildCustomAnalyzer("ik_smart")
                // 特殊标签过滤
                .addCharFilter("html_strip")
                // 去除停用词
                .addTokenFilter(stopFilter)
                .build(keywords);
        try {
            AnalyzeResponse analyzeResponse = restHighLevelClient.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
            List<AnalyzeResponse.AnalyzeToken> analyzeTokens = analyzeResponse.getTokens();
            analyzeTokens.forEach(analyzeToken -> {
                analyzeTerms.add(analyzeToken.getTerm());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return analyzeTerms;
    }

}
