package com.eskeli.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Class Desc : 搜索（参数）实体，用于Web层参数接收、传递
 *
 * @author elilson.s
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class IdxParam {

    // 索引名称（显式指定索引名称），若不指定，由系统依据规则自动生成 { indexName }
    private String indexName;
    // 索引方式（0:不覆盖索引，1：覆盖索引）
    private Integer indexWay  = 0;
    // 发布方式（0：全量发布，1：增量发布）
    private Integer publishWay = 0;
    // 业务种类
    private String businessType;
    // 业务数据批次标识（businessBatchIds），增量发布索引时，按批次发布数据
    private String[] businessBatchIds;
    // 检索数据模板对象列表
    private List<SearchFieldInformation> searchFieldInformationList;

}
