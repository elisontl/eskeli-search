package com.eskeli.search.exprocess;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Class Desc : CheckedConsumer 异常检查处理
 *
 * @author elison.s
 */
public class CheckedConsumerProcessor {

    /**
     * Performs this operation on the given argument.
     * @param checkedConsumer the input argument
     */
    public static <T> Consumer<T> accept(CheckedConsumer<T> checkedConsumer) {
        Objects.nonNull(checkedConsumer);
        return i -> {
            try {
                checkedConsumer.accept(i);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        };
    }

}
