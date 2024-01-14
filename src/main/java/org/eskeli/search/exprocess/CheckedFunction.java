package org.eskeli.search.exprocess;

/**
 * Class Desc : 异常检查函数接口，对JDK中Function<T, R>接口进行扩展
 *
 * @author elisontl
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws Exception
     */
    R apply(T t) throws Exception;

}
