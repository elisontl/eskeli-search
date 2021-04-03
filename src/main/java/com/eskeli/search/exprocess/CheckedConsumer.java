package com.eskeli.search.exprocess;

/**
 * Class Desc : 异常检查函数接口，对JDK中Consumer<T>接口进行扩展
 *
 * @author elison.s
 */
@FunctionalInterface
public interface CheckedConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(T t) throws Exception;

}
