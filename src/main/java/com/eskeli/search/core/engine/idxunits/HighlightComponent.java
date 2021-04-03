package com.eskeli.search.core.engine.idxunits;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import java.lang.reflect.Field;

/**
 * Class Desc : 类组件工具
 *
 * @author elison.s
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HighlightComponent {

    private Field[] highlightFields;

    private String highlightFieldNames;

    private SearchSourceBuilder searchSourceBuilder;

}
