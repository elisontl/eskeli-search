package com.eskeli.search.annotation;

import java.lang.annotation.*;

/**
 * Default Keli-Search Idx Engity
 *
 * @author elison.s
 */
@KeliSearchIdxEntity(wholeAreaIndex = true, indexCover = true)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeliSearchDefaultEntity {

}
