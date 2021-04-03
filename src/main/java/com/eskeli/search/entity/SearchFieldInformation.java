package com.eskeli.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class Desc : 数据实体模板
 *
 * @author elison.s
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SearchFieldInformation {

    // 字段名称
    private String fieldName;
    // 权重（加权值）
    private float boost;
    // 存储 (1:是，0:否）
    private boolean isStore;

}
