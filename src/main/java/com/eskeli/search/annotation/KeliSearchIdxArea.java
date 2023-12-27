package com.eskeli.search.annotation;

import java.lang.annotation.*;

/**
 * Class Desc : 索引域注解
 * 1) 字段上指定该注解，表示该字段进行索引，如果类和字段上都无注解的属性，不索引
 * 2）默认情况，首字母小写的所修饰的字段名称，通过指定alias可修改字段索引名
 *    优先级( 高于 > )类级别注解 @KeliSearchIdxEntity
 *
 * @author elisontl
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface KeliSearchIdxArea {

    /**
     * 索引域别名
     * @return
     */
    String alias() default "";

    /**
     * 索引
     * @return
     */
    boolean index() default true;

    /**
     * 分词
     * @return
     */
    boolean analyzed() default true;

    /**
     * 存储 ( 存一份指定域的原始数据 ）
     * @return
     */
    boolean store() default true;

    /**
     * 加权
     * @return
     */
    float boost() default 1.0f;

    /**
     * 高亮
     * @return
     */
    boolean highlight() default true;

}
