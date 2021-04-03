package com.eskeli.search.core.func;

import com.eskeli.search.entity.SearchFieldInformation;
import org.elasticsearch.common.xcontent.XContentBuilder;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Class Desc : 索引域属性填充接口
 *
 * @author elison.s
 */
@FunctionalInterface
public interface EnrichFieldPropertiesFunction {

    /**
     * 生成索域引映射mappings方法
     *
     * @param field  : Field : 索引的字段
     * @param contentBuilder : XContentBuilder : mappings对象
     * @param searchFieldInformationMap : Map<String, THSearchFieldInformation> searchFieldInformationMap : 索引域配置参数Map
     * @return
     */
    XContentBuilder execute(Field field,
                            XContentBuilder contentBuilder,
                            Map<String, SearchFieldInformation> searchFieldInformationMap) throws IOException;

}
