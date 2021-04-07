package com.eskeli.search.annotation;

import java.lang.annotation.*;

/**
 * Class Desc : 索引类注解（类级别注解）
 *
 * 注解使用方式：类上指定该注解，表示该类是索引类，可通过本注解控制字段的索引策略
 *
 *  全域索引：（除去配置 @KeliSearchNotIdxArea 注解的字段外，所有字段全部索引，以下两种场景都按全域索引处理）
 *  <1> 实体类指定该注解，且 wholeAreaIndex为true
 *  <2> 实体类不指定该注解（不推荐，一般建议指定索引注解，以对索引进行更精细化控制）
 *
 *  非全域索引：（只索引实体类中配有 @KeliSearchIdxArea 的字段）
 *  <1> 实体类指定该注解，且 wholeFieldIndex 为 false（默认方式）
 *
 * @author elison.s
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface KeliSearchIdxEntity {

    /**
     * 索引别名（可通过指定alias可指定索引别名，为索引生成新名字）
     * @return
     */
    String alias() default "";

    /**
     * 是否为全字段索引
     * （默认为非全字段索引，只索引实体类中打有 @KeliSearchIdxArea 注解的字段）
     * @return
     */
    boolean wholeAreaIndex() default false;

    /**
     * 是否进行索引覆盖
     * (如果当前创建的索引已经存在，判断是否对其进行索引覆盖）
     * true : 覆盖掉原索引，重新创建新索引；false : 不覆盖，使用原来索引
     * @return
     */
    boolean indexCover() default false;

    /**
     * 索引映射（mapping）创建方式
     * 1) 方式一: true : 采用ElasticSearch Dynamic创建
     * 2）方式二: false : 采用自研中间件创建
     */
    boolean createMappingWay() default false;

    /**
     * 索引类的type
     * （同一种索引类，可细分为更多类别type，该type通过该属性动态指定）
     */
    String indexType() default "";

    /**
     * 索引分片数
     * @return
     */
    int shardsNumber() default 0;

    /**
     * 索引副本数
     * @return
     */
    int replicasNumber() default 0;

}
