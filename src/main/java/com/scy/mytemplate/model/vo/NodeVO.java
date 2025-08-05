// FILE: src/main/java/com/scy/mytemplate/model/vo/NodeVO.java
package com.scy.mytemplate.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * 节点视图对象 (Node View Object)。
 * 作为数据传输层，用于向前端安全地返回节点的脱敏信息。
 */
@Data
public class NodeVO implements Serializable {
    /**
     * 节点的唯一名称，通常与关联的图片 `storagePath` 一致。
     */
    private String name;

    /**
     * 节点的属性集合。
     * 从 Neo4j 查询返回后，已自动排除了 `name` 属性，以避免数据冗余。
     */
    private Map<String, Object> properties;

    private static final long serialVersionUID = 1L;
}
// END OF FILE: src/main/java/com/scy/mytemplate/model/vo/NodeVO.java