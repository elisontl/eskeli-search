package com.eskeli.search.factories;

import com.eskeli.search.annotation.KeliSearchIdxEntity;
import com.eskeli.search.constant.KeliSearchConstant;
import com.eskeli.search.entity.IdxComponent;
import com.eskeli.search.utils.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * Class Desc : 索引（组件）工厂
 *
 * @author elison.s
 */
public class IdxComponentFactory {

    /**
     * 自动获取索引名
     *
     * @param clazz : 任意类型Class对象
     * 返回值说明:
     * 返回类型为 IndexNameModel，其中属性 indexName 为生成的索引名, indexClazzObject 参数为 clazz 所对应实体对象上的 @KeliSearchIdxEntity 注解对象
     * @return
     */
    public static <T> IdxComponent generateIndexName(Class<?> clazz)  {
        // 参数校验
        if (clazz == null) {
            return null;
        }
        Map<String, KeliSearchIdxEntity> resultMap = new HashMap<>();
        // 校验是否为索引类
        KeliSearchIdxEntity indexClazz = clazz.getDeclaredAnnotation(KeliSearchIdxEntity.class);
        String alias = (indexClazz != null) ? indexClazz.alias() : null;
        // 获取索引名
        String indexName = (StringUtils.isNotEmpty(alias) ? alias.toLowerCase() : convert2IndexName(clazz.getSimpleName()));

        indexName = KeliSearchConstant.DATA_INDEX_PREFIX + indexName;

        return new IdxComponent(indexName, indexClazz);
    }

    /**
     * 转换索引名
     *
     * @param str
     * @return
     */
    public static String convert2IndexName(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        StringBuffer result = new StringBuffer();
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isUpperCase(chars[i])) {
                result = (i == 0) ? result.append(Character.toLowerCase(chars[i]))
                        : result.append("_").append(Character.toLowerCase(chars[i]));
            } else {
                result.append(chars[i]);
            }
        }
        return result.toString();
    }

    /**
     * 字符串首字母转小写
     *
     * @param str
     * @return
     */
    public static String toLowerCaseFirstLetter(String str) {
        if (Character.isLowerCase(str.charAt(0))) {
            return str;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(str.charAt(0))).append(str.substring(1)).toString();
        }
    }

}
