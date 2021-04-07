
import com.eskeli.search.connection.ConnectClient;
import com.eskeli.search.core.SearchContext;
import com.eskeli.search.core.engine.GeneralSearchEngine;
import com.eskeli.search.entity.SearchParam;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import java.io.IOException;
import java.util.*;

/**
 * Class Desc ：单元测试类
 */
@Slf4j
public class C_01 {

    public static void main(String[] args) {

        search();

        //publish();

    }

    public static void publish() {

        RestHighLevelClient client = ConnectClient.SingleRestConnectClient.getSearchClient("192.168.1.121", 9201, "http");

        SearchContext searchContext = new SearchContext(
            new GeneralSearchEngine(client)
        );

        // boolean del_result = searchContext.deleteIndex(Book.class);

        // log.info("----- 删除索引:" + del_result + " ----- ");

        // 注意: 这里的参数穿的是实体对象，非 class 对象

        List<Book> list = new ArrayList<Book>() {
            {
                add(new Book(1L, "平凡的世界", "平凡的世界"));
                add(new Book(2L, "回过头更好", "回过头更好"));
                add(new Book(3L, "简洁的巅峰", "简洁的巅峰"));
            }
        };

        Set<Book> set = new HashSet<Book>() {
            {
                add(new Book(1L, "平凡的世界", "平凡的世界"));
                add(new Book(2L, "回过头更好", "回过头更好"));
            }
        };

//        boolean publish_result = searchContext.publishIndexData(list);

        boolean publish_result = searchContext.publishIndexData("test_set_book", list, null);

        log.info("----- 发布结果: " + publish_result + " ----- ");

    }

    public static void search() {

        RestHighLevelClient client = ConnectClient.SingleRestConnectClient.getSearchClient("192.168.1.121", 9201, "http");

        SearchContext searchContext = new SearchContext(
                new GeneralSearchEngine(client)
        );

        // 统一搜索
        List<Map<String, Object>> list = searchContext.executeGlobalSearch(new SearchParam());

        log.info(" ------ hello lombok, log back ----- ");

        list.forEach(map -> {

            log.info(map.toString());
        });

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
