package com.eskeli.search.annotation;

import java.lang.annotation.*;

@KeliSearchIdxEntity(wholeAreaIndex = true, indexCover = true)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeliSearchDefaultEntity {

}
