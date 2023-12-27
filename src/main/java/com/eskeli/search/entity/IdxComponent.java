package com.eskeli.search.entity;

import com.eskeli.search.annotation.KeliSearchIdxEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Class Desc : 索引（分解）元件存储模型
 *
 * @author elisontl
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class IdxComponent {

    // 索引名
    private String indexName;

    // 所索引类上的 @KeliSearchIdxEntity 注解对象
    private KeliSearchIdxEntity indexClazzObject;

}
