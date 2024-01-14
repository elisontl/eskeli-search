package org.eskeli.search.connection;

import org.eskeli.search.utils.PropertiesUtils;
import org.eskeli.search.utils.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import java.util.stream.Stream;

/**
 * Class Desc : 高级别连接客户端 （ 适配单节点模式、集群模式 ）
 *
 * @author elisontl
 */
public class ConnectClient {

    // Connect client private construct
    private ConnectClient() {

    }

    // Inner class rest connect client
    public static class SingleRestConnectClient {

        // Rest high level connect client
        private static RestHighLevelClient restHighLevelClient = null;

        /**
         * Get搜索链接客户端（默认方法），单节点模式
         *
         * @return
         */
        public static RestHighLevelClient getSearchClient(String settingsPath) {
            if (restHighLevelClient == null) {
                // 加载参数配置
                PropertiesUtils propUtil = PropertiesUtils.getInstance().loadConfig(settingsPath);
                // 连接模式开关
                String multiConnectWay = propUtil.getProperty("isAssemblage");
                /* 集群连接模式
                 （连接适配：若开关开启状态下，仍获取到集群连接模式连接参数为空，自动适配为单节点模式连接） */
                if (StringUtils.isNotEmpty(multiConnectWay)) {
                    String multiConnectParam = propUtil.getProperty("assemblageKey");
                    String[] multiConnects = StringUtils.isNotEmpty(multiConnectParam) ? multiConnectParam.split(",") :
                            new String[] { org.apache.commons.lang3.StringUtils.join(
                                    propUtil.getProperty("host"),
                                    ":",
                                    propUtil.getProperty("port"),
                                    ":",
                                    propUtil.getProperty("protocol")
                            ) };
                    restHighLevelClient = getSearchClient(multiConnects);
                }
                // Single node method
                else {
                    restHighLevelClient = getSearchClient(
                            propUtil.getProperty("host"),
                            (StringUtils.isNotEmpty(propUtil.getProperty("port"))) ? Integer.parseInt(propUtil.getProperty("port")) : 0,
                            propUtil.getProperty("protocol")
                    );
                }
            }
            return restHighLevelClient;
        }

        /**
         * Get搜索链接客户端，单节点模式
         *
         * @param host : String : 主机
         * @param port : String : 端口
         * @param protocol : String : 协议
         * @return
         */
        public static RestHighLevelClient getSearchClient(String host, Integer port, String protocol) {
            if (restHighLevelClient == null) {
                // 若未指定协议，默认访问协议为 http
                if (StringUtils.isEmpty(protocol)) {
                    protocol = "http";
                }
                restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(host, port, protocol)));
            }
            return restHighLevelClient;
        }

        /**
         * 搜索链接客户端（多节点集群连接模式，适配指定协议或不指定协议场景）
         *
         * @param conns : String : 连接字串数组
         *  参数说明:
         *  1）不指定协议
         *  new String[] {"ip1:port1", "ip2:port2", "ip3:port3"} // 这里不做多维数组，使用约定格式，便于调用理解
         * 2）指定协议
         * new String[] {"ip1:port1:protocol1","ip2:port2:protocol2", "ip3:port3:protocol3"}
         *
         * @return
         */
        public static synchronized RestHighLevelClient getSearchClient(String... conns) {
            if (restHighLevelClient == null) {
                // String[] 》2 》HttpHost[]
                HttpHost[] httpHosts = Stream.of(conns).map(conn -> {
                    String[] params = conn.split(":");
                    return (params.length > 2) ? new HttpHost(params[0], Integer.parseInt(params[1]), params[2]) : new HttpHost(params[0], Integer.parseInt(params[1]));
                }).toArray(value -> new HttpHost[value]);
                // Create RestHighLevelClient Obj
                restHighLevelClient = new RestHighLevelClient(RestClient.builder(httpHosts));
            }
            return restHighLevelClient;
        }
    }

}
