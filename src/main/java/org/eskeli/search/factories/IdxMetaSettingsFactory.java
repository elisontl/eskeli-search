package org.eskeli.search.factories;

import org.eskeli.search.utils.StringUtils;
import org.elasticsearch.common.settings.Settings;
import java.util.Map;
import java.util.Set;

/**
 * Class Desc : 索引（Settings）工厂
 *
 * @author elisontl
 */
public class IdxMetaSettingsFactory {

    /**
     * 生成索引Settings对象，默认方式
     *
     * @return
     */
    public static Settings generateIndexSettings() {
        Settings settings = Settings.builder()
            // 配置索引分分片数，分片副本数
            .put("number_of_shards", "5").put("number_of_replicas", "2").build();
        return settings;
    }

    /**
     * 生成索引Settings对象，两参数形式
     *
     * @param shardsNumber : Integer : 索引分片数
     * @param replicasNumber : Integer : 索引副本数
     * @return
     */
    public static Settings generateIndexSettings(Integer shardsNumber, Integer replicasNumber) {
        Settings settings = Settings.builder()
            // 配置索引分分片数，分片副本数
            .put("number_of_shards", shardsNumber != null ? String.valueOf(shardsNumber) : "5")
            .put("number_of_replicas", replicasNumber != null ? String.valueOf(replicasNumber) : "2").build();
        return settings;
    }

    /**
     * 生成索引Settings对象，三参数形式
     *
     * @param clusterName : String : 索引集群名称
     * @param shardsNumber : Integer : 索引分片数
     * @param replicasNumber : Integer : 索引副本数
     * @return
     */
    public static Settings generateIndexSettings(String clusterName, Integer shardsNumber, Integer replicasNumber) {
        Settings settings = Settings.builder()
            // 配置索引集群名称，索引分分片数，分片副本数
            .put("cluster.name", StringUtils.isNotEmpty(clusterName) ? clusterName : "el-es" + Math.random())
            .put("number_of_shards", shardsNumber != null ? String.valueOf(shardsNumber) : "5")
            .put("number_of_replicas", replicasNumber != null ? String.valueOf(replicasNumber) : "2").build();
        return settings;
    }

    /**
     * 生成索引Settings对象，无限参数形式
     *
     * @param paramMap : Map<String, String> : 索引设置参数
     * @return
     */
    public static Settings generateIndexSettings(Map<String, String> paramMap) {
        Settings.Builder settingBuilder = Settings.builder();
        Set<String> keys = paramMap.keySet();
        keys.forEach(key -> {
            settingBuilder.put(key, paramMap.get(key));
        });
        return settingBuilder.build();
    }

}
