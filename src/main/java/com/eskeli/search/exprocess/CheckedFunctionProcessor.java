package com.eskeli.search.exprocess;

import java.util.Objects;
import java.util.function.Function;

/**
 * Class Desc : CheckedFunction异常检查处理
 *
 * @author elison.s
 */
public class CheckedFunctionProcessor {

    /**
     * 异常处理
     *
     * @param function
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Function<T, R> apply(CheckedFunction<T, R> function) {
        Objects.nonNull(function);
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

}
