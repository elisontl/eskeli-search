package com.eskeli.search.core.func;

import java.util.Map;

/**
 * Class Desc : 转换索引源接口
 *
 * @author elison.s
 */
@FunctionalInterface
public interface ConvertSourceToMapFunction<T, U> {

    /**
     * 转换索引源，字段类型适配
     *
     * @param t : T : 任意类型对象
     * @return
     */
    Map<String, U> convert(T t);

}
