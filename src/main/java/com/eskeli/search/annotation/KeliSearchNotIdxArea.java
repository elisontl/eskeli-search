package com.eskeli.search.annotation;

import java.lang.annotation.*;

/**
 * Class Desc : 非索引字段注解
 * （该注解主要用于实体类进行全字段索引时，个别字段不参加索引的情况）
 *
 * @author elison.s
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KeliSearchNotIdxArea {
    // 标记注解
}
