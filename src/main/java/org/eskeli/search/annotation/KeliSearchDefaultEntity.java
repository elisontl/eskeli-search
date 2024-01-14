package org.eskeli.search.annotation;

import java.lang.annotation.*;

/**
 * Default Keli-Search Idx Engity
 *
 * @author elisontl
 */
@KeliSearchIdxEntity(wholeAreaIndex = true, indexCover = true)
@Retention(RetentionPolicy.RUNTIME)
public @interface KeliSearchDefaultEntity {

}
